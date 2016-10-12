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

import java.nio.charset.StandardCharsets

import net.sandius.rembulan.ByteString
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

    def go(l: ByteString, expected: ByteString): Unit = {
      describe (l.toString) {
        it ("is parsed correctly") {
          val v = StringLiteral.fromString(l).value()
          v mustEqual expected
        }
      }
    }

    def raw(s: String) = ByteString.fromRaw(s)
    def j(s: String) = ByteString.of(s)
    def utf(s: String) = ByteString.of(s, StandardCharsets.UTF_8)

    go (raw("\"hello\""), utf("hello"))
    go (raw("'there'"), utf("there"))

    go (raw("\"doub'le\""), utf("doub'le"))
    go (raw("'sing\"le'"), utf("sing\"le"))

    go (raw("\"esc\\n\""), utf("esc\n"))
    go (raw("\"a\\tb\\r\""), utf("a\tb\r"))
    go (raw("\"\f\b\""), utf("\f\b"))

    go (raw("\"\\104\\101\\108\\32\\108\\111\""), utf("hel lo"))

    go (raw("\"\\x68\\101\\x6c\\x6Co\""), utf("hello"))

    go (raw("\"[\\000123456789]\""), utf("[\000123456789]"))

    go (raw("\"\\u{68}\\u{69}"), utf("hi"))
    go (raw("\"\\u{00FA}d\\u{011B}sn\\u{00FD}\""), utf("úděsný"))

  }

}
