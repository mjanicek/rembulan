/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    try {
      val result = parser.Expr()
      parser.Eof()

      println("--> " + exprToString(result))
      println()
    }
    catch {
      case ex: TokenMgrError => throw new ParseException(ex.getMessage)
    }
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
        intercept[ParseException] {
          tryParseExpr(s)
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
