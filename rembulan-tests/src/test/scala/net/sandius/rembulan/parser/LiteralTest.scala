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

import net.sandius.rembulan.parser.ast.{Numeral, StringLiteral}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class LiteralTest extends FunSpec with MustMatchers {

  describe ("numeral") {

    def testNumeral(s: String, exp: Numeral): Unit = {
      describe (s) {
        it ("is parsed correctly") {
          val n = Numeral.fromString(s)
          n mustEqual exp
        }
      }
    }

    def integer(l: String, expected: Long): Unit = {
      testNumeral(l, new Numeral.IntegerNumeral(expected))
    }

    def float(l: String, expected: Double): Unit = {
      testNumeral(l, new Numeral.FloatNumeral(expected))
    }

    integer ("0x7fffffffffffffff", 9223372036854775807L)
    integer ("0x8000000000000000", -9223372036854775808L)

    integer ("0x9000000000000000", -8070450532247928832L)
    integer ("0x90000000000000000", 0)
    integer ("0x1000000000000000000000000001", 1)
    integer ("0x999999999999999999999999999999999999000000000000000", -8070450532247928832L)

  }

  describe ("string literal") {

    def go(l: String, expected: String): Unit = {
      describe (l) {
        it ("is parsed correctly") {
          val v = StringLiteral.fromString(l).value()
          v mustEqual expected
        }
      }
    }

    go ("\"hello\"", "hello")
    go ("'there'", "there")

    go ("\"doub'le\"", "doub'le")
    go ("'sing\"le'", "sing\"le")

    go ("\"esc\\n\"", "esc\n")
    go ("\"a\\tb\\r\"", "a\tb\r")
    go ("\"\f\b\"", "\f\b")

    go ("\"\\104\\101\\108\\32\\108\\111\"", "hel lo")

    go ("\"\\x68\\101\\x6c\\x6Co\"", "hello")

    go ("\"\\u{68}\\u{69}", "hi")

  }

}
