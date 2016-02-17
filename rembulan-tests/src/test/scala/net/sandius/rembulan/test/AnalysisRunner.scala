package net.sandius.rembulan.test

import java.io.PrintWriter

import com.github.mdr.ascii.graph.Graph
import com.github.mdr.ascii.layout._
import net.sandius.rembulan.compiler.gen.block.{Entry, Exit, Node}
import net.sandius.rembulan.compiler.gen.{SuffixingClassNameGenerator, FlowIt, SlotState}
import net.sandius.rembulan.compiler.{gen => rembulan}
import net.sandius.rembulan.lbc.{Prototype, PrototypePrinter, PrototypePrinterVisitor}
import net.sandius.rembulan.parser.LuaCPrototypeLoader

import scala.collection.JavaConversions._

object AnalysisRunner {

  import Util._

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

    object Upvalues3 extends Fragment {
      code =
          """local x, y
            |if g then
            |  y = function() return x end
            |else
            |  x = function() return y end
            |end
            |return x or y
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

    object FunctionCalls extends Fragment {
      code =
          """local function f(x, y)
            |  return x + y
            |end
            |
            |return f(1, 2)
          """
    }

    object FunctionCalls2 extends Fragment {
      code =
          """local function abs(x)
            |  local function f(x, acc)
            |    if x > 0 then
            |      return f(x - 1, acc + 1)
            |    elseif x < 0 then
            |      return f(x + 1, acc + 1)
            |    else
            |      return acc
            |    end
            |  end
            |  local v = f(x, 0)
            |  return v
            |end
            |
            |return abs(20)
          """
    }

    object FunctionCalls3 extends Fragment {
      code =
          """local function abs(x)
            |  local function f(g, x, acc)
            |    if x > 0 then
            |      return g(g, x - 1, acc + 1)
            |    elseif x < 0 then
            |      return g(g, x + 1, acc + 1)
            |    else
            |      return acc
            |    end
            |  end
            |  local v = f(f, x, 0)
            |  return v
            |end
            |
            |return abs(20)
          """
    }

    object LocalUpvalue extends Fragment {
      code =
          """local function f()
            |  local x = 1
            |  local function g()
            |    return x * x
            |  end
            |  return g()
            |end
            |
            |return f()  -- equivalent to return 1
          """
    }

    object ReturningAFunction extends Fragment {
      code =
          """local function f()
            |  return function(x) return not not x, x end
            |end
            |
            |return f()()
          """

    }

  }

  def unitForPrototype(flow: FlowIt, prototype: Prototype): rembulan.Unit = {
    (flow.units().find { _.prototype == prototype }).get
  }

  def printFlow(unit: rembulan.Unit, flow: FlowIt, main: Boolean = true): Unit = {
    val proto = unit.prototype
    val cp = unit.generic()

    println((if (main) "Main" else "Child") + " (" + PrototypePrinter.pseudoAddr(proto) + "): " + unit.name)

    def slotsToString(slots: SlotState): String = {
      Option(slots) match {
        case Some(s) => "[" + s.toString(",\t") + "]"
        case None => "(none)"
      }
    }

    def inValue(node: Node): String = {
      node match {
        case ent: Entry => ent.arguments().toString
        case n => slotsToString(n.inSlots())
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
      override def toString = {
        val in = inValue(node)
        val out = outValue(node)

        val inOut = Util.tabulate(("in:  " + in) :: ("out: " + out) :: Nil, "  ", "\t")
        val ns = node.toString

        val width = math.max((inOut map { _.length }).max, (ns.split("\n") map { _.length }).max)

        val sep = Util.fillStr("-", width)

        inOut.mkString("\n") + "\n" + sep + "\n" + ns
      }
    }

    val flowGraph = cp.nodeGraph
    val vs = flowGraph.vertices().toSet
    val es = flowGraph.edges().toSet map { p: net.sandius.rembulan.util.Pair[Node, Node] => (p.first, p.second) }

    val graph = Graph(
      vertices = vs map { MyNode },
      edges = (es map { case (u, v) => (MyNode(u), MyNode(v)) }).toList
    )

//    println("Resume points:")
//    for (r: ResumptionPoint <- flow.resumePoints.toSet) {
//      println("\t" + r.toString + " : " + "[" + flowStates(r) + "]")
//    }

    val ascii = timed("Graph layout") {
      GraphLayout.renderGraph(graph)
    }

    println()
    println("Type: " + cp.functionType().toExplicitString)
    println()

    val outcalls = cp.callSites.toMap mapValues { _.toSet }
    val ocs = for (p <- outcalls.keys; args <- outcalls(p)) yield (p, args)

    if (ocs.isEmpty) {
      println("No static in-chunk calls.")
    }
    else {
      println(ocs.size + " static in-chunk call(s):")
      for ((p, args) <- ocs) {
        println("\tto  " + unitForPrototype(flow, p).name + "  with  (" + args.toString + ")")
      }
    }

    println()
    println(ascii)

  }

  def main(args: Array[String]): Unit = {

    import fragments._

    val luacPath = "luac53"
    require (luacPath != null)

    val ploader = new LuaCPrototypeLoader(luacPath)

    println(ploader.getVersion)
    println("------------")

    val program = ReturningAFunction

    println(program.code)

    val proto = ploader.load(program.code)

    proto.accept(new PrototypePrinterVisitor(new PrintWriter(System.out)))
//    PrototypePrinter.print(proto, new PrintWriter(System.out))

    println()
    println("Control flow")
    println("------------")
    println()

    val flow = timed("Analysis") {
      val f = new FlowIt(proto, new SuffixingClassNameGenerator("test"))
      f.go()
      f
    }

    for (u <- flow.units.toSeq.sortBy { _.name }) {
      println()
      printFlow(u, flow, u.prototype == proto)
    }

  }

}
