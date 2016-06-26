package net.sandius.rembulan.parser

import java.io.{ByteArrayInputStream, PrintWriter}

import net.sandius.rembulan.compiler.analysis.{BranchInlinerVisitor, TypeInfo, TyperVisitor}
import net.sandius.rembulan.compiler.ir.Branch
import net.sandius.rembulan.compiler._
import net.sandius.rembulan.compiler.util.{BlocksSimplifier, IRPrinterVisitor, TempUseVerifierVisitor}
import net.sandius.rembulan.parser.analysis.NameResolutionTransformer
import net.sandius.rembulan.parser.ast.{Chunk, Expr}
import net.sandius.rembulan.test._
import org.junit.runner.RunWith
import org.scalatest.{FunSpec, MustMatchers}
import org.scalatest.junit.JUnitRunner

import scala.annotation.tailrec
import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class IRTranslationTest extends FunSpec with MustMatchers {

  val bundles = Seq(
    BasicFragments,
    BasicLibFragments,
    CoroutineFragments,
    DebugLibFragments,
    IOLibFragments,
    MathFragments,
    MetatableFragments,
    OperatorFragments,
    StringFragments,
    TableLibFragments
  )

  def parseExpr(s: String): Expr = {
    val bais = new ByteArrayInputStream(s.getBytes)
    val parser = new Parser(bais)
    val result = parser.Expr()
    parser.Eof()
    result
  }

  def parseChunk(s: String): Chunk = {
    val bais = new ByteArrayInputStream(s.getBytes)
    val parser = new Parser(bais)
    parser.Chunk()
  }

  def resolveNames(c: Chunk): Chunk = {
    new NameResolutionTransformer().transform(c)
  }

  def verify(fn: IRFunc): Unit = {
    val visitor = new BlocksVisitor(new TempUseVerifierVisitor())
    visitor.visit(fn)
  }

  def insertCpuAccounting(fn: IRFunc): IRFunc = {
    val visitor = new CPUAccountingVisitor(CPUAccountingVisitor.INITIALISE)
    visitor.visit(fn.blocks())
    fn.update(visitor.result())
  }

  def collectCpuAccounting(fn: IRFunc): IRFunc = {
    val visitor = new CPUAccountingVisitor(CPUAccountingVisitor.COLLECT)
    visitor.visit(fn.blocks())
    fn.update(visitor.result())
  }

  def assignTypes(fn: IRFunc): TypeInfo = {
    val visitor = new TyperVisitor()
    visitor.visit(fn)
    visitor.valTypes()
  }

  def inlineBranches(fn: IRFunc, types: TypeInfo): IRFunc = {
    val visitor = new BranchInlinerVisitor(types)
    visitor.visit(fn)
    fn.update(visitor.result())
  }

  def printTypes(types: TypeInfo): Unit = {
    for (v <- types.vals().asScala) {
      println(v + " -> " + types.typeOf(v))
    }
  }

  def printBlocks(blocks: Blocks): Unit = {
    val pw = new PrintWriter(System.out)
    val printer = new IRPrinterVisitor(pw)
    printer.visit(blocks)
    pw.flush()

    println()
  }

  describe ("expression") {

    describe ("can be translated to IR:") {

      for ((s, o) <- Expressions.get if o) {

        val ss = "return " + s

        it (ss) {
          val ck = resolveNames(parseChunk(ss))

          val modBuilder = new ModuleBuilder()
          val translator = new IRTranslatorTransformer(modBuilder)
          translator.transform(ck)
          val mod = modBuilder.build()
          for (fn <- mod.fns().asScala) {
            println("Function [" + fn.id + "]")
            println()
            printBlocks(fn.blocks)
            verify(fn)
          }
        }

      }

    }

  }

  case class CompiledFn(fn: IRFunc, types: TypeInfo)
  case class CompiledModule(fns: Seq[CompiledFn])

  def translate(chunk: Chunk): Module = {
    val resolved = resolveNames(chunk)
    val modBuilder = new ModuleBuilder()
    val translator = new IRTranslatorTransformer(modBuilder)
    translator.transform(resolved)
    modBuilder.build()
  }

  def compile(fn: IRFunc): CompiledFn = {
    val withCpu = insertCpuAccounting(fn)

    def pass(fn: IRFunc, types: TypeInfo): IRFunc = {
      val withCpu = collectCpuAccounting(fn)
      val inlined = inlineBranches(withCpu, types)
      val filtered = inlined.update(BlocksSimplifier.filterUnreachableBlocks(inlined.blocks()))
      val merged = filtered.update(BlocksSimplifier.mergeBlocks(filtered.blocks()))
      merged
    }

    @tailrec
    def loop(fn: IRFunc): (IRFunc, TypeInfo) = {
      val types = assignTypes(fn)
      val opt = pass(fn, types)
      if (fn == opt) {
        (opt, types)
      }
      else {
        loop(opt)
      }
    }

    val (fnl, types) = loop(withCpu)
    CompiledFn(fnl, types)
  }

  def compile(mod: Module): CompiledModule = {
    CompiledModule(for (fn <- mod.fns().asScala) yield compile(fn))
  }

  for (b <- bundles) {
    describe ("from " + b.name + " :") {
      for (f <- b.all) {
        describe (f.description) {
          it ("can be translated to IR") {
            val code = f.code

            println("--BEGIN--")
            println(code)
            println("---END---")

            val ir = translate(parseChunk(code))
            val cmod = compile(ir)

            def printCompiledFn(cfn: CompiledFn): Unit = {
              println("Function [" + cfn.fn.id + "]" + (if (cfn.fn.id.isRoot) " (main)" else ""))
              println()

              println("Nested refs:")
              if (cfn.fn.nested().isEmpty) {
                println("\t(none)")
              }
              else {
                val ids = cfn.fn.nested().asScala
                println("\t" + (ids map { id => "[" + id + "]"}).mkString(", "))
              }
              println()

              println("Blocks:")
              printBlocks(cfn.fn.blocks)
              println()

              println("Value types:")
              printTypes(cfn.types)
              println()
            }

            for (cfn <- cmod.fns) {
              printCompiledFn(cfn)
            }

          }
        }
      }
    }
  }


}
