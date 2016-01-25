package net.sandius.rembulan.test

import java.io.PrintWriter

import com.github.mdr.ascii.graph.Graph
import com.github.mdr.ascii.layout._
import net.sandius.rembulan.compiler.gen.FlowIt
import net.sandius.rembulan.compiler.gen.block.Node
import net.sandius.rembulan.lbc.{Prototype, PrototypePrinter, PrototypePrinterVisitor}
import net.sandius.rembulan.parser.LuaCPrototypeLoader

import scala.collection.JavaConversions._

object AnalysisRunner {

  def printFlow(proto: Prototype, main: Boolean = true): Unit = {
    if (main) {
      println("Main (" + PrototypePrinter.pseudoAddr(proto) + "):")
    }
    else {
      println()
      println("Child (" + PrototypePrinter.pseudoAddr(proto) + "):")
    }

    val flow = new FlowIt(proto)
    flow.go()

    val flowEdges = flow.reachabilityGraph.toMap
    val flowStates = flow.slots.toMap

    case class MyNode(node: Node) {
      require (node != null)
      override def toString = {
        val ios = flowStates(node)
        val in = if (ios.in != null) "[ " + ios.in.toString + " ]" else "(none)"
        val out = if (ios.out != null) "[ " + ios.out.toString + " ]" else "(none)"

        "   " + in + " ->\n" + node.toString + "\n-> " + out + "   "
      }
    }

    val vertices = flowEdges.keySet map { MyNode }
    val in = (for ((n, es) <- flowEdges) yield (es.in.toSet map { e: Node => (e, n) })).flatten
    val out = (for ((n, es) <- flowEdges) yield (es.out.toSet map { e: Node => (n, e) })).flatten
//      val edges = (in ++ out).toSet
    val edges0 = (in ++ out).toSet
    val edges = edges0 map { case (u, v) => (MyNode(u), MyNode(v)) }


    val graph = Graph(
      vertices = vertices,
      edges = edges.toList
    )

    val ascii = GraphLayout.renderGraph(graph)
    println()
    println(ascii)

    val it = proto.getNestedPrototypes.iterator()
    while (it.hasNext) {
      val child = it.next()
      printFlow(child, false)
    }
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
    """
      |local x = {}
      |for i = 0, 10 do
      |  if i % 2 == 0 then x[i // 2] = function() return i, x end end
      |end
    """.stripMargin

/*
    val program =
    """
      |local x
      |x = 1
      |
      |local function sqr()
      |  return x * x
      |end
      |
      |x = 3
      |return sqr()
    """.stripMargin
*/

/*
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
*/

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

    printFlow(proto)

  }

}
