package net.sandius.rembulan.test

import com.github.mdr.ascii.graph.Graph
import com.github.mdr.ascii.layout._
import java.io.PrintWriter
import net.sandius.rembulan.compiler.gen.ControlFlowTraversal
import net.sandius.rembulan.lbc.{Prototype, PrototypePrinterVisitor, PrototypePrinter}
import net.sandius.rembulan.parser.LuaCPrototypeLoader
import net.sandius.rembulan.util.IntBuffer

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

object AnalysisRunner {

  def fillStr(pattern: String, width: Int): String = {
    val bld = new StringBuilder()
    while (bld.length < width) {
      val rem = width - bld.length
      bld.append(pattern.substring(0, math.min(rem, pattern.length)))
    }
    bld.toString()
  }

  def padRight(s: String, width: Int): String = {
    if (s.length >= width) s else s + fillStr(" ", width - s.length)
  }

  def tabulate(lines: Seq[String], tab: String): Seq[String] = {
    val tabbedLines = for (l <- lines) yield l.split("\t").toSeq
    val numCols = (tabbedLines map { _.size }).max
    val matrix = tabbedLines map { tl => tl ++ Seq.fill[String](numCols - tl.size)("") }

    val cols = matrix.transpose
    val colWidths = for (col <- cols) yield (col map { _.length }).max

    val padded = for (row <- matrix) yield for ((cell, width) <- row zip colWidths) yield padRight(cell, width)
    for (row <- padded) yield row.mkString(tab)
  }

  def main(args: Array[String]): Unit = {

    val luacPath = "luac53"
    require (luacPath != null)

    val ploader = new LuaCPrototypeLoader(luacPath)

    println(ploader.getVersion)
    println("------------")

//    val program =
//      """
//        |if x >= 0 and x <= 10 then print(x) end
//      """.stripMargin

    val program =
    """do
      |  local a = 0
      |  local b = 1
      |end
      |
      |do
      |  local a = 2
      |end
      |
      |function f(x)
      |  print(x)
      |  if x > 0 then
      |    return f(x - 1)
      |  else
      |    if x < 0 then
      |      return f(x + 1)
      |    else
      |      return 0
      |    end
      |  end
      |end
      |
      |local hi = "hello."
      |
      |function g()
      |  print(hi)
      |  for i = 0, 10 do
      |    coroutine.yield(function() return i end)
      |  end
      |end
      |
      |return f(3),f(-2)
      """.stripMargin

//    val program =
//      """local f = function (x, y)
//        |    return x + y
//        |end
//        |return -1 + f(1, 3) + 39
//      """.stripMargin

//    val program =
//      """local f = function (x, y, z)
//        |    return x + y + z
//        |end
//        |return -1 + f(1, 1, 2) + 39
//      """.stripMargin

    println(program)

    val proto = ploader.load(program)

    proto.accept(new PrototypePrinterVisitor(new PrintWriter(System.out)))
//    PrototypePrinter.print(proto, new PrintWriter(System.out))

    println()
    println("Control flow")
    println("------------")
    println()

    def intBufferToSeq(ib: IntBuffer): IndexedSeq[Int] = for (i <- 0 to ib.length() - 1) yield ib.get(i)

    def printTraversal(proto: Prototype, main: Boolean = true): Unit = {
      if (main) {
        println("Main (" + PrototypePrinter.pseudoAddr(proto) + "):")
      }
      else {
        println()
        println("Child (" + PrototypePrinter.pseudoAddr(proto) + "):")
      }

      /*
       Possible types:

       * ... any
       N ... nil
       B ... boolean
       lU ... lightuserdata
       nI ... integer
       nF ... float
       S ... string
       T ... table
       F ... function
       U ... userdata
       C ... thread
      */

      val params = ArrayBuffer.empty[String]
      for (i <- 1 to proto.getNumberOfParameters) {
        params.append("*")
      }
      if (proto.isVararg) {
        params.append("...")
      }

//      println("Parameters: (" + params.mkString(" ") + ")")


      val trav = new ControlFlowTraversal(proto)

//      trav.print(System.out)

      val blox = trav.getBlocks.toSeq

      case class Bk(idx: Int) {
        override def toString = {
          val lines = trav.blockToString(blox(idx), "", true).split("\n").toSeq
          val tabulated = tabulate(lines, "  ").toList

          val blk = "Block #" + idx
          val underline = fillStr("-", (tabulated map { _.length }).max)

          (blk :: underline :: tabulated).mkString("\n")
        }
      }

      case object Entry {
        override def toString = {
          val firstLine = "Entry"
          val signature = params.mkString("(", " ", ")")

          val sep = fillStr("-", ((firstLine :: signature :: Nil) map { _.length }).max)

          (firstLine :: sep :: signature :: Nil).mkString("\n")
        }
      }
      case object Exit

      val vertices = blox.indices.toSet
      val edges = for (v <- vertices; nx <- intBufferToSeq(blox.get(v).next) if nx >= 0) yield (Bk(v), Bk(nx))
      val exitEdges = for (v <- vertices if blox.get(v).next.contains(-1)) yield (Bk(v), Exit)

      val graph = Graph(
        vertices = Set(Entry, Exit) ++ (blox.indices map Bk),
        edges = (Entry -> Bk(0)) :: (edges.toList ::: exitEdges.toList)
      )

      val ascii = GraphLayout.renderGraph(graph)
      println()
      println(ascii)

      val it = proto.getNestedPrototypes.iterator()
      while (it.hasNext) {
        val child = it.next()
        printTraversal(child, false)
      }
    }

    printTraversal(proto)

  }

}
