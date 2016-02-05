package net.sandius.rembulan.test

import java.io.PrintWriter

import com.github.mdr.ascii.graph.Graph
import com.github.mdr.ascii.layout._
import net.sandius.rembulan.compiler.gen.{Slots, FlowIt}
import net.sandius.rembulan.compiler.gen.block.{Entry, Exit, Node}
import net.sandius.rembulan.lbc.{Prototype, PrototypePrinter, PrototypePrinterVisitor}
import net.sandius.rembulan.parser.LuaCPrototypeLoader

import scala.collection.JavaConversions._

object AnalysisRunner {

  object fragments {

    object IfThenElse extends Fragment {
      code =
          """if x >= 0 and x <= 10 then print(x) end
          """
    }

    object Upvalues1 extends Fragment {
      code =
          """
            |local x = {}
            |for i = 0, 10 do
            |  if i % 2 == 0 then x[i // 2] = function() return i, x end end
            |end
          """
    }

    object Upvalues2 extends Fragment {
      code =
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
          """
    }


    object BlockLocals extends Fragment {
      code =
          """do
            |  local a = 0
            |  local b = 1
            |end
            |
            |do
            |  local a = 2
            |end
          """
    }

    object Tailcalls extends Fragment {
      code =
          """function f(x)
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
            |return f(3),f(-2)
          """
    }

    object FuncWith2Params extends Fragment {
      code =
          """local f = function (x, y)
            |    return x + y
            |end
            |return -1 + f(1, 3) + 39
          """
    }

    object FuncWith3Params extends Fragment {
      code =
          """local f = function (x, y, z)
            |    return x + y + z
            |end
            |return -1 + f(1, 1, 2) + 39
          """
    }

    object DeterminateVarargs extends Fragment {
      code =
          """local a, b = ...
            |if a > 0 then
            |  return b, a
            |else
            |  return a, b
            |end
          """
    }

    object IndeterminateVarargs extends Fragment {
      code =
          """local a = ...
            |if a then
            |  return ...
            |else
            |  return false, ...
            |end
          """
    }

    object NilTestInlining extends Fragment {
      code =
          """local a
            |if a then
            |  return true
            |else
            |  return false
            |end
          """
    }

    object VarargFunctionCalls extends Fragment {
      code =
          """local f = function(...) return ... end
            |return true, f(...)
          """
    }

    object VarargFunctionCalls2 extends Fragment {
      code =
          """local x = ...
            |local f = function(...) return ... end
            |if x then
            |  return f(...)
            |else
            |  return true, f(...)
            |end
          """
    }

  }

  def printFlow(proto: Prototype, main: Boolean = true): Unit = {
    if (main) {
      println("Main (" + PrototypePrinter.pseudoAddr(proto) + "):")
    }
    else {
      println()
      println("Child (" + PrototypePrinter.pseudoAddr(proto) + "):")
    }

    val before = System.nanoTime()
    val flow = new FlowIt(proto)
    flow.go()
    val after = System.nanoTime()

    System.out.println("Analysis took %.1f ms".format((after - before) / 1000000.0))

    val flowEdges = flow.reachabilityGraph.toMap
//    val flowStates = flow.slots.toMap

    def slotsToString(slots: Slots): String = {
      Option(slots) match {
        case Some(s) => s"[$s]"
        case None => "(none)"
      }
    }

    def inValue(node: Node): String = {
      node match {
        case ent: Entry => ent.arguments().toString
        case n => slotsToString(n.outSlots())
      }
    }

    def outValue(node: Node): String = {
      node match {
        case ex: Exit => ex.returnType().toString
        case n => slotsToString(n.outSlots())
      }
    }

    case class MyNode(node: Node) {
      require (node != null)
      override def toString = "  " + inValue(node) + " -> " + outValue(node) + "  \n" + node.toString
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

//    println("Resume points:")
//    for (r: ResumptionPoint <- flow.resumePoints.toSet) {
//      println("\t" + r.toString + " : " + "[" + flowStates(r) + "]")
//    }

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

    import fragments._

    val luacPath = "luac53"
    require (luacPath != null)

    val ploader = new LuaCPrototypeLoader(luacPath)

    println(ploader.getVersion)
    println("------------")

    val program = VarargFunctionCalls2

    println(program.code)

    val proto = ploader.load(program.code)

    proto.accept(new PrototypePrinterVisitor(new PrintWriter(System.out)))
//    PrototypePrinter.print(proto, new PrintWriter(System.out))

    println()
    println("Control flow")
    println("------------")
    println()

    printFlow(proto)

  }

}
