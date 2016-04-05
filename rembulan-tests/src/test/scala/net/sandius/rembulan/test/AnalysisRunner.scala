package net.sandius.rembulan.test

import java.io.{BufferedOutputStream, FileOutputStream, PrintWriter}

import com.github.mdr.ascii.graph.Graph
import com.github.mdr.ascii.layout._
import net.sandius.rembulan.compiler.gen._
import net.sandius.rembulan.compiler.gen.block.{Entry, Exit, Node}
import net.sandius.rembulan.lbc.{Prototype, PrototypePrinter, PrototypePrinterVisitor}
import net.sandius.rembulan.parser.LuaCPrototypeLoader

import scala.collection.JavaConversions._

object AnalysisRunner {

  import Util._

  def unitForPrototype(flow: Chunk, prototype: Prototype): CompilationUnit = {
    (flow.units().find { _.prototype == prototype }).get
  }

  def printFlow(unit: CompilationUnit, flow: Chunk, main: Boolean = true): Unit = {
    val proto = unit.prototype
    val cp = unit.generic()

    println((if (main) "Main" else "Child") + " (" + PrototypePrinter.pseudoAddr(proto) + "): " + unit.name)

    case class MyNode(node: Node) {
      require (node != null)

      def slotsToString(slots: SlotState) = if (slots != null) "[ " + slots.toString(",\t") + " ]" else "(none)"

      def in = node match {
        case ent: Entry => ent.arguments().toString
        case n => slotsToString(n.inSlots())
      }

      def out = node match {
        case ex: Exit => ex.returnType().toString
        case n => slotsToString(n.outSlots())
      }

      override def toString = {
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
    println("Type:")
    println("\t" + cp.functionType().toExplicitString)
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

  def dumpToFile(cc: CompiledClass): Unit = {
    val filename = cc.name() + ".class"

    println("Dumping \"" + cc.name() + "\" to \"" + filename + "\"...")

    val bytes = cc.bytes().copyToNewArray()

    val bos = new BufferedOutputStream(new FileOutputStream(filename, false))
    Stream.continually(bos.write(bytes))
    bos.close()
  }

  val loader = new LuaCFragmentCompiler("luac53")

  def main(args: Array[String]): Unit = {

    import BasicFragments._

    val program = Upvalues3
    val proto = loader.compile(program)

    section("LuaC version") {
      println(loader.version)
    }

    section("Program code") {
      println(program.code)
    }

    section("Lua bytecode listing") {
      proto.accept(new PrototypePrinterVisitor(new PrintWriter(System.out)))
    }

    val compiler = new ChunkCompiler(new SuffixingClassNameGenerator("test"))

    section("Compilation") {

      val flow = timed("Compile") {
        compiler.compile(proto)
      }

      // write classes to files
      for (cc <- flow.classes()) {
        dumpToFile(cc)
      }

      for (u <- flow.units.toSeq.sortBy { _.name }) {
        println(separator)
        println()
        printFlow(u, flow, u.prototype == proto)
      }

    }

  }

}
