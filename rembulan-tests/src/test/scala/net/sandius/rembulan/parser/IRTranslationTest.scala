package net.sandius.rembulan.parser

import java.io.{ByteArrayInputStream, PrintWriter}

import net.sandius.rembulan.compiler.IRTranslatorTransformer
import net.sandius.rembulan.compiler.ir.IRNode
import net.sandius.rembulan.compiler.util.IRPrinterVisitor
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

  describe ("expression") {

    describe ("can be translated to IR:") {

      for ((s, o) <- Expressions.get if o) {

        val ss = "return " + s

        it (ss) {
          val ck = resolveNames(parseChunk(ss))

          val translator = new IRTranslatorTransformer()
          translator.transform(ck)

          val ns = translator.nodes().asScala.toList

          val pw = new PrintWriter(System.out)
          val printer = new IRPrinterVisitor(pw)

          for (n <- ns) {
            n.accept(printer)
          }
          pw.flush()

          println()
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

            val ns = translator.nodes().asScala.toList

            val pw = new PrintWriter(System.out)
            val printer = new IRPrinterVisitor(pw)

            for (n <- ns) {
              n.accept(printer)
            }
            pw.flush()

            println()
          }
        }
      }
    }
  }


}
