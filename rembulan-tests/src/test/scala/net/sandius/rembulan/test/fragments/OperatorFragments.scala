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

package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.{FragmentBundle, FragmentExpectations, OneLiners}

object OperatorFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  val Arithmetic = "perform arithmetic on"
  val Bitwise = "perform bitwise operation on"
  val Length = "get length of"
  val Concatenate = "concatenate"

  object opArgErrors {

//    val ValidBinArgs = Seq("1", "1.0", "\"1\"", "\"1.0\"")
    val ValidBinArgs = Seq("1")

    trait Tester {

      def op: String
      def kind: String
      def exps(iv: String): Iterable[String]

      def testprogram (codePrefix: String, expr: String): String = {
        val pfx = if (codePrefix != null && !codePrefix.isEmpty) codePrefix + "; " else ""
        pfx + "return " + expr
      }

      def expectedError(tpe: String) = {
        "attempt to " + kind + " a " + tpe + " value"
      }

      def apply(invalidArg: String, tpe: String, codePrefix: String = null): Unit = {
        val err = expectedError(tpe)
        for (e <- exps(invalidArg)) {
          program (testprogram (codePrefix, e)) failsWith err
        }
      }

    }

    class BinaryOpTester(val op: String, val kind: String, valids: Iterable[String]) extends Tester {
      override def exps(iv: String) = (for (v <- valids) yield s"$v $op $iv" :: s"$iv $op $v" :: Nil).flatten
    }

    class UnaryOpTester(val op: String, val kind: String) extends Tester {
      override def exps(iv: String) = op + iv :: Nil
    }

    def binary(op: String, kind: String, valids: Iterable[String] = ValidBinArgs): Unit = {
      val test = new BinaryOpTester(op, kind, valids)

      test("nil", "nil")
      test("true", "boolean")
      test("false", "boolean")
      test("\"\"", "string")
      test("\"x\"", "string")
      test("{}", "table")

      test("x", "nil")
      test("x", "nil", "local x")
      test("x", "table", "x = {}")
      test("x.y", "nil", "x = {}")
    }

    def unary(op: String, kind: String): Unit = {
      val test = new UnaryOpTester(op, kind)

      test("nil", "nil")
      test("true", "boolean")
      test("false", "boolean")
      test("\"\"", "string")
      test("\"x\"", "string")
      test("{}", "table")

      test("x", "nil")
      test("x", "nil", "local x")
      test("x", "table", "x = {}")
      test("x.y", "nil", "x = {}")
    }

  }

  in (EmptyContext) {

    about ("add") {

      program ("return 1 + 2") succeedsWith 3
      program ("return 1 + 2.0") succeedsWith 3.0
      program ("return 1.0 + 2.0") succeedsWith 3.0
      program ("return -0.0 + 0") succeedsWith 0.0
      program ("return -0.0 + 0.0") succeedsWith 0.0
      program ("return 1 + \"2\"") succeedsWith 3.0
      program ("return \"1\" + 0") succeedsWith 1.0

      program ("return (1 / 0) + 1") succeedsWith Double.PositiveInfinity
      program ("return (-1 / 0) + 1") succeedsWith Double.NegativeInfinity
      program ("return (1 / 0) + (-1 / 0)") succeedsWith NaN
      program ("return (-1 / 0) + (-1 / 0)") succeedsWith Double.NegativeInfinity

      opArgErrors.binary("+", Arithmetic)

    }

    about ("sub") {

      program ("return 1 - 2") succeedsWith -1
      program ("return 1 - 2.0") succeedsWith -1.0
      program ("return 1.0 - 2.0") succeedsWith -1.0
      program ("return -0.0 - 0") succeedsWith -0.0
      program ("return -0.0 - 0.0") succeedsWith -0.0
      program ("return 1 - \"2\"") succeedsWith -1.0
      program ("return \"1\" - 0") succeedsWith 1.0

      program ("return (1 / 0) - 1") succeedsWith Double.PositiveInfinity
      program ("return (-1 / 0) - 1") succeedsWith Double.NegativeInfinity
      program ("return (1 / 0) - (-1 / 0)") succeedsWith Double.PositiveInfinity
      program ("return (-1 / 0) - (-1 / 0)") succeedsWith NaN

      opArgErrors.binary("-", Arithmetic)

    }

    about ("idiv") {

      program ("return 3 // 2") succeedsWith 1
      program ("return 3.0 // 2") succeedsWith 1.0
      program ("return 3 // -2") succeedsWith -2
      program ("return 3 // -2.0") succeedsWith -2.0
      program ("return \"3\" // 2") succeedsWith 1.0
      program ("return \"3\" // \"-2\"") succeedsWith -2.0

      program ("return 3.0 // 0") succeedsWith Double.PositiveInfinity
      program ("return 3.0 // -0") succeedsWith Double.PositiveInfinity
      program ("return 3.0 // -0.0") succeedsWith Double.NegativeInfinity

      program ("return 3 // 0") failsWith "attempt to divide by zero"

      opArgErrors.binary("//", Arithmetic)

    }

    about ("div") {

      program ("return 3 / 0") succeedsWith Double.PositiveInfinity
      program ("return 3 / -0") succeedsWith Double.PositiveInfinity
      program ("return -3 / -0") succeedsWith Double.NegativeInfinity
      program ("return -3 / -0.0") succeedsWith Double.PositiveInfinity

      opArgErrors.binary("/", Arithmetic)

    }

    about ("mod") {

      program ("return 3 % 2") succeedsWith 1
      program ("return 3 % -2") succeedsWith -1
      program ("return 3.0 % 2") succeedsWith 1.0
      program ("return 3.0 % -2") succeedsWith -1.0

      program ("return 3 % 0") failsWith "attempt to perform 'n%0'"

      program ("return 3.0 % 0") succeedsWith NaN

      opArgErrors.binary("%", Arithmetic)

    }

    about ("unm") {

      program ("return -1") succeedsWith -1
      program ("return -0.0") succeedsWith 0.0
      program ("return --0") succeedsWith ()

      program ("return -\"1\"") succeedsWith -1.0

      opArgErrors.unary("-", Arithmetic)

    }

    about ("shl") {

      program ("return 0 << \"0\"") succeedsWith 0
      program ("return -0.0 << 1") succeedsWith 0

      program ("return 3 << 0") succeedsWith 3

      program ("return 1 << 63") succeedsWith Long.MinValue
      program ("return (1 << 63) - 1") succeedsWith Long.MaxValue

      program ("return 1 << 64") succeedsWith 0

      program ("return 6 << 1000") succeedsWith 0

      program ("return 3 << 1") succeedsWith 6
      program ("return 3.0 << 1") succeedsWith 6
      program ("return \"3\" << 1") succeedsWith 6

      program ("return 3 << 1.5") failsWith "number has no integer representation"
      program ("return 3.5 << 0") failsWith "number has no integer representation"

      program ("return 5 << 2.0") succeedsWith 20
      program ("return 5.0 << 2.0") succeedsWith 20
      program ("return 5.0 << 2") succeedsWith 20

      program ("return 7 << -1") succeedsWith 3

      opArgErrors.binary("<<", Bitwise)

    }

    about ("shr") {

      program ("return 0 >> 5") succeedsWith 0
      program ("return 1 >> 100000") succeedsWith 0

      program ("return 1 >> 1.2") failsWith "number has no integer representation"
      program ("return 10.1 >> 0") failsWith "number has no integer representation"

      program ("return -1 >> 1") succeedsWith Long.MaxValue
      program ("return (-1 >> 1) + 1") succeedsWith Long.MinValue

      program ("return 9 >> 2.0") succeedsWith 2
      program ("return 9.0 >> 2.0") succeedsWith 2
      program ("return 9.0 >> 2") succeedsWith 2

      program ("return 5 >> -2") succeedsWith 20

      opArgErrors.binary("<<", Bitwise)

    }

    about ("band") {

      program ("return 0 & 0") succeedsWith 0
      program ("return 10 & 3") succeedsWith 2

      program ("return 255 & -1") succeedsWith 255
      program ("return 255 & -255") succeedsWith 1
      program ("return -1 & 50") succeedsWith 50
      program ("return -1 & -3") succeedsWith -3

      program ("return 10.0 & 3") succeedsWith 2
      program ("return 10 & 3.0") succeedsWith 2
      program ("return 10.0 & 3.0") succeedsWith 2

      program ("return \"13\" & 6") succeedsWith 4
      program ("return 13 & \"6\"") succeedsWith 4

      program ("return 13 & 1.5") failsWith "number has no integer representation"
      program ("return 1.2 & 255") failsWith "number has no integer representation"

      opArgErrors.binary("&", Bitwise)

    }

    about ("bor") {

      program ("return 0 | 0") succeedsWith 0
      program ("return 10 | 3") succeedsWith 11

      program ("return 2 | -1") succeedsWith -1
      program ("return 1 | -255") succeedsWith -255
      program ("return -1 | -10") succeedsWith -1

      program ("return 10.0 | 3") succeedsWith 11
      program ("return 10 | 3.0") succeedsWith 11
      program ("return 10.0 | 3.0") succeedsWith 11

      program ("return \"13\" | 6") succeedsWith 15
      program ("return 13 | \"6\"") succeedsWith 15

      program ("return 13 | 1.5") failsWith "number has no integer representation"
      program ("return 1.2 | 255") failsWith "number has no integer representation"

      opArgErrors.binary("|", Bitwise)

    }

    about ("bxor") {

      program ("return 1 ~ 0") succeedsWith 1
      program ("return 1 ~ 1") succeedsWith 0
      program ("return 10 ~ 3") succeedsWith 9

      program ("return 2 ~ -1") succeedsWith -3
      program ("return 1 ~ -255") succeedsWith -256
      program ("return -1 ~ -10") succeedsWith 9

      program ("return 10.0 ~ 3") succeedsWith 9
      program ("return 10 ~ 3.0") succeedsWith 9
      program ("return 10.0 ~ 3.0") succeedsWith 9

      program ("return \"13\" ~ 6") succeedsWith 11
      program ("return 13 ~ \"6\"") succeedsWith 11

      program ("return 13 ~ 1.5") failsWith "number has no integer representation"
      program ("return 1.2 ~ 255") failsWith "number has no integer representation"

      opArgErrors.binary("~", Bitwise)

    }

    about ("bnot") {

      program ("return ~0") succeedsWith -1
      program ("return ~-1") succeedsWith 0

      program ("return ~(-1 >> 1)") succeedsWith Long.MinValue
      program ("return ~(-1 << 63)") succeedsWith Long.MaxValue

      program ("return ~3.6") failsWith "number has no integer representation"
      program ("return ~\"10\"") succeedsWith -11
      program ("return ~~35") succeedsWith 35
      program ("return ~~35.0") succeedsWith 35

      opArgErrors.unary("~", Bitwise)

    }

    about ("len") {

      program ("return #''") succeedsWith 0
      program ("return #'hello'") succeedsWith 5

      program ("return #{}") succeedsWith 0
      program ("return #{3, 2, 1, 0}") succeedsWith 4

      // errors & origin reporting
      val errTest = new opArgErrors.UnaryOpTester("#", Length)

      errTest("nil", "nil")
      errTest("true", "boolean")
      errTest("false", "boolean")
      errTest("1", "number")
      errTest("0.0", "number")

      errTest("x", "nil")
      errTest("x", "nil", "local x")
      errTest("x", "number", "x = 1")
      errTest("x.y", "nil", "x = {}")

    }

    about ("concat") {

      program ("return 0 .. 1") succeedsWith "01"
      program ("return 0.0 .. -1") succeedsWith "0.0-1"
      program ("return '0' .. 'x'") succeedsWith "0x"
      program ("return '' .. ''") succeedsWith ""
      program ("return (0/0)..(0/0)") succeedsWith "nannan"
      program ("return (1/0)..(-1/0)") succeedsWith "inf-inf"

      program ("return 1 .. 2 .. 3") succeedsWith "123"
      program ("return 'a'..'b'..'c'..'d'") succeedsWith "abcd"

      // errors & origin reporting
      val errTest = new opArgErrors.BinaryOpTester("..", Concatenate, Seq("''"))

      errTest("nil", "nil")
      errTest("true", "boolean")
      errTest("false", "boolean")
      errTest("{}", "table")

      errTest("x", "nil")
      errTest("x", "nil", "local x")
      errTest("x", "table", "x = {}")
      errTest("x.y", "nil", "x = {}")

    }

    about ("lt") {

      program ("return 1 < 2") succeedsWith true
      program ("return 1 < 1") succeedsWith false

      program ("return 'a' < 'b'") succeedsWith true
      program ("return 'ab' < 'a'") succeedsWith false
      program ("return 'ab' > 'a'") succeedsWith true

      program ("return 0 < 0.0") succeedsWith false
      program ("return '0' < '0.0'") succeedsWith true

      program ("return x < y") failsWith "attempt to compare two nil values"
      program ("return 1 < x") failsWith "attempt to compare number with nil"
      program ("return 1 < false") failsWith "attempt to compare number with boolean"
      program ("return true < false") failsWith "attempt to compare two boolean values"
      program ("return {} < 1") failsWith "attempt to compare table with number"
      program ("return {} < {}") failsWith "attempt to compare two table values"

      program ("return '0' < 1") failsWith "attempt to compare string with number"
      program ("return '0' > 1") failsWith "attempt to compare number with string"

    }

    about ("le") {

      program ("return 1 <= 2") succeedsWith true
      program ("return 1 <= 1") succeedsWith true

      program ("return 'a' <= 'b'") succeedsWith true
      program ("return 'ab' <= 'a'") succeedsWith false
      program ("return 'ab' >= 'a'") succeedsWith true

      program ("return 0 <= 0.0") succeedsWith true
      program ("return '0' <= '0.0'") succeedsWith true

      program ("return x <= y") failsWith "attempt to compare two nil values"
      program ("return 1 <= x") failsWith "attempt to compare number with nil"
      program ("return 1 <= false") failsWith "attempt to compare number with boolean"
      program ("return true <= false") failsWith "attempt to compare two boolean values"
      program ("return {} <= 1") failsWith "attempt to compare table with number"
      program ("return {} <= {}") failsWith "attempt to compare two table values"

      program ("return '0' <= 1") failsWith "attempt to compare string with number"
      program ("return '0' >= 1") failsWith "attempt to compare number with string"

    }

    about ("eq") {

      program ("return 0 == 0") succeedsWith true
      program ("return nil == nil") succeedsWith true
      program ("return 0 == '0'") succeedsWith false

      program ("return {} == {}") succeedsWith false
      program ("local x = {}; y = x; return x == y") succeedsWith true

      program ("return 'x' == 'x'") succeedsWith true
      program ("return 'x'..'y' == 'xy'") succeedsWith true

      program ("return 0 == 0.0") succeedsWith true
      program ("return -0.0 == 0.0") succeedsWith true

      program ("return (1 / 0) == (1 / 0)") succeedsWith true
      program ("return (-1 / 0) == (-1 / 0)") succeedsWith true
      program ("return (0 / 0) == (0 / 0)") succeedsWith false

    }

  }

}
