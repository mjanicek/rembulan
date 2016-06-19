package net.sandius.rembulan.parser

import java.io.{ByteArrayInputStream, PrintWriter}

import net.sandius.rembulan.compiler.IRTranslatorTransformer
import net.sandius.rembulan.compiler.ir.IRNode
import net.sandius.rembulan.compiler.util.IRPrinterVisitor
import net.sandius.rembulan.parser.ast.Expr
import org.junit.runner.RunWith
import org.scalatest.{FunSpec, MustMatchers}
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class IRTranslationTest extends FunSpec with MustMatchers {

  def parseExpr(s: String): Expr = {
    val bais = new ByteArrayInputStream(s.getBytes)
    val parser = new Parser(bais)
    val result = parser.Expr()
    parser.Eof()
    result
  }

  describe ("expression") {

    describe ("can be translated to IR:") {

      for ((s, o) <- Expressions.get if o) {

        it (s) {

          val expr = parseExpr(s)

          val translator = new IRTranslatorTransformer()
          expr.accept(translator)

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
