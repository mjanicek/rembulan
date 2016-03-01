package net.sandius.rembulan.test

import java.io.PrintWriter

import com.github.mdr.ascii.graph.Graph
import com.github.mdr.ascii.layout._
import net.sandius.rembulan.compiler.gen.block.{Entry, Exit, Node}
import net.sandius.rembulan.compiler.gen._
import net.sandius.rembulan.compiler.{gen => rembulan}
import net.sandius.rembulan.lbc.{Prototype, PrototypePrinter, PrototypePrinterVisitor}
import net.sandius.rembulan.parser.LuaCPrototypeLoader

import scala.collection.JavaConversions._

object AnalysisRunner {

  import Util._

  def unitForPrototype(flow: CompiledChunk, prototype: Prototype): CompilationUnit = {
    (flow.units().find { _.prototype == prototype }).get
  }

  def printFlow(unit: CompilationUnit, flow: CompiledChunk, main: Boolean = true): Unit = {
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

    val fields = {
      val uvds = unit.prototype.getUpValueDescriptions
      (for (i <- 0 until uvds.size) yield uvds.get(i)).toList
    }

    fields match {
      case Nil =>
        println("No fields.")
      case fs =>
        println("Fields:")
        for (uvd <- fs) {
          println("\tupvalue: (" + uvd.toString + ")")
        }
    }
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

    import BasicFragments._

    val luacPath = "luac53"
    require (luacPath != null)

    val ploader = new LuaCPrototypeLoader(luacPath)

    println(ploader.getVersion)
    println("------------")

    val program = JustAdd

    println(program.code)

    val proto = ploader.load(program.code)

    proto.accept(new PrototypePrinterVisitor(new PrintWriter(System.out)))
//    PrototypePrinter.print(proto, new PrintWriter(System.out))

    val compiler = new ChunkCompiler()

    println()
    println("Control flow")
    println("------------")
    println()

    val flow = timed("Compile") {
      compiler.compile(proto, new SuffixingClassNameGenerator("test"))
    }

    for (u <- flow.units.toSeq.sortBy { _.name }) {
      println()
      printFlow(u, flow, u.prototype == proto)
    }

  }

}
