package net.sandius.rembulan.parser

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintWriter}

import net.sandius.rembulan.parser.ast.Expr
import net.sandius.rembulan.parser.util.FormattingPrinterVisitor
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class ExpressionParsingTest extends FunSpec with MustMatchers {

  def tryParseExpr(s: String): Unit = {
    println("\"" + s + "\"")

    val bais = new ByteArrayInputStream(s.getBytes)
    val parser = new Parser(bais)
    val result = parser.Expr()
    parser.Eof()

    println("--> " + exprToString(result))
    println()
  }

  def exprToString(expr: Expr): String = {
    val baos = new ByteArrayOutputStream()
    val pw = new PrintWriter(baos)
    val visitor = new FormattingPrinterVisitor(pw)
    expr.accept(visitor)
    pw.flush()
    String.valueOf(baos.toByteArray map { _.toChar })
  }

  describe ("expr") {

    def ok(s: String): Unit = {
      it ("ok: " + s) {
        tryParseExpr(s)
      }
    }

    def nok(s: String): Unit = {
      it ("not-ok: " + s) {
        try {
          tryParseExpr(s)
        }
        catch {
          case ex: ParseException =>
            println("--> (error:) " + ex.getMessage)
        }
      }
    }

    for ((s, good) <- Expressions.get) {
      good match {
        case true => ok(s)
        case false => nok(s)
      }
    }

  }

}
