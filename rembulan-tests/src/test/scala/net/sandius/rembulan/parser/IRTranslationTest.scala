package net.sandius.rembulan.parser

import java.io.{ByteArrayInputStream, PrintWriter}

import net.sandius.rembulan.compiler.analysis.{BranchInlinerVisitor, TyperVisitor}
import net.sandius.rembulan.compiler.ir.Branch
import net.sandius.rembulan.compiler.{Blocks, BlocksVisitor, IRTranslatorTransformer}
import net.sandius.rembulan.compiler.util.{IRPrinterVisitor, TempUseVerifierVisitor}
import net.sandius.rembulan.parser.analysis.NameResolutionTransformer
import net.sandius.rembulan.parser.ast.{Chunk, Expr}
import net.sandius.rembulan.test._
import org.junit.runner.RunWith
import org.scalatest.{FunSpec, MustMatchers}
import org.scalatest.junit.JUnitRunner

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

  def verify(blocks: Blocks): Unit = {
    val visitor = new BlocksVisitor(new TempUseVerifierVisitor())
    visitor.visit(blocks)
  }

  def assignTypes(blocks: Blocks): Unit = {
    val typer = new TyperVisitor()
    typer.visit(blocks)
    val types = typer.valTypes()

    for (v <- types.vals().asScala) {
      println(v + " -> " + types.typeOf(v))
    }

    println()

    def branchToString(b: Branch): String = {
      "branch [" + b.jmpDest() + " | " + b.next() + "]"
    }

    val handler = new BranchInlinerVisitor.InlineHandler {
      override def inlineAsTrue(b: Branch) = {
        println(branchToString(b) + " can be inlined to TRUE (" + b.jmpDest() + ")")
      }
      override def inlineAsFalse(b: Branch) = {
        println(branchToString(b) + " can be inlined to FALSE (" + b.next() + ")")
      }
      override def noInline(b: Branch) = {
        println(branchToString(b) + " cannot be inlined")
      }
    }
    val inliner = new BlocksVisitor(new BranchInlinerVisitor(types, handler))

    inliner.visit(blocks)
  }

  describe ("expression") {

    describe ("can be translated to IR:") {

      for ((s, o) <- Expressions.get if o) {

        val ss = "return " + s

        it (ss) {
          val ck = resolveNames(parseChunk(ss))

          val translator = new IRTranslatorTransformer()
          translator.transform(ck)
          val blocks = translator.blocks()

          val pw = new PrintWriter(System.out)
          val printer = new IRPrinterVisitor(pw)
          printer.visit(blocks)
          pw.flush()

          println()

          verify(blocks)
        }

      }

    }

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

            val ck = resolveNames(parseChunk(code))

            val translator = new IRTranslatorTransformer()
            translator.transform(ck)
            val blocks = translator.blocks()

            val pw = new PrintWriter(System.out)
            val printer = new IRPrinterVisitor(pw)
            printer.visit(blocks)
            pw.flush()

            println()

            verify(blocks)
            assignTypes(blocks)
          }
        }
      }
    }
  }


}
