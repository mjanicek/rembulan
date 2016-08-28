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

import net.sandius.rembulan.core._
import net.sandius.rembulan.test.{FragmentBundle, FragmentExpectations, OneLiners}
import net.sandius.rembulan.{LuaFormat, core => lua}

object StringLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  val StringSetsMetatable = fragment ("StringSetsMetatable") {
    """return getmetatable("hello")
    """
  }
  StringSetsMetatable in StringLibContext succeedsWith (classOf[Table])

  val StringTableIsReferencedInStringMetatable = fragment ("StringTableIsReferencedInStringMetatable") {
    """local mt = getmetatable("hello")
      |return mt == string, mt.__index == string
    """
  }
  StringTableIsReferencedInStringMetatable in StringLibContext succeedsWith (false, true)

  val LenMethodIsEqualToLenOperator = fragment ("LenMethodIsEqualToLenOperator") {
    """local hello = "hello"
      |return hello:len() == #hello
    """
  }
  LenMethodIsEqualToLenOperator in StringLibContext succeedsWith (true)

  in (StringLibContext) {

    about ("byte") {
      program ("""return ("hello"):byte(1)""") succeedsWith (104)
      program ("""return ("hello"):byte(-1)""") succeedsWith (111)
      program ("""return ("hello"):byte(5)""") succeedsWith (111)
      program ("""return ("hello"):byte(-5)""") succeedsWith (104)

      program ("""return ("hello"):byte(0)""") succeedsWith ()
      program ("""return ("hello"):byte(100)""") succeedsWith ()
      program ("""return ("hello"):byte(-100)""") succeedsWith ()

      program ("""return ("hello"):byte(1, -1)""") succeedsWith (104, 101, 108, 108, 111)
      program ("""return ("hello"):byte(-1, 1)""") succeedsWith ()
      program ("""return ("hello"):byte(2, 4)""") succeedsWith (101, 108, 108)
      program ("""return ("hello"):byte(2, -2)""") succeedsWith (101, 108, 108)
      program ("""return ("hello"):byte(0, 100)""") succeedsWith (104, 101, 108, 108, 111)
      program ("""return ("hello"):byte()""") succeedsWith (104)
      program ("""return (""):byte()""") succeedsWith ()

      program ("""return string.byte(42, 1, -1)""") succeedsWith (52, 50)
      program ("""return string.byte(42.0, 1, -1)""") succeedsWith (52, 50, 46, 48)
      program ("""return ("hello"):byte("1")""") succeedsWith (104)
      program ("""return ("hello"):byte("1.0")""") succeedsWith (104)
      program ("""return ("hello"):byte(1.0)""") succeedsWith (104)

      program ("""local x; return ("Boom"):byte(x)""") succeedsWith (66)
      program ("""return ("Boom"):byte(true)""") failsWith (classOf[IllegalArgumentException], "bad argument #"<<"1">>" to 'byte' (number expected, got boolean)")
    }

    about ("char") {
      program ("""return string.char()""") succeedsWith ("")
      program ("""return string.char(104, 101, 108, 108, 111)""") succeedsWith ("hello")

      program ("""return string.char("104", "105.0", 33.0)""") succeedsWith ("hi!")

      program ("""string.char(-1)""") failsWith (classOf[IllegalArgumentException], "bad argument #1 to 'char' (value out of range)")
      program ("""string.char(256)""") failsWith (classOf[IllegalArgumentException], "bad argument #1 to 'char' (value out of range)")
    }

    about ("format") {
      program ("""return ("%s%d"):format("0", 10.0)""") succeedsWith ("010")

      program ("""return ("%%u%%"):format()""") succeedsWith ("%u%")

      program ("""local x; return ("%"):format(x)""") failsWith (classOf[IllegalArgumentException], "invalid option '%<\\0>' to 'format'")
      program ("""return ("%"):format()""") failsWith (classOf[IllegalArgumentException], "bad argument #"<<"1">>" to 'format' (no value)")

      program ("""return ("%d"):format()""") failsWith (classOf[IllegalArgumentException], "bad argument #"<<"1">>" to 'format' (no value)")
      program ("""return ("%d"):format("hi")""") failsWith (classOf[IllegalArgumentException], "bad argument #"<<"1">>" to 'format' (number expected, got string)")
      program ("""return ("%d"):format("1")""") succeedsWith ("1")
      program ("""return ("%d"):format(1.2)""") failsWith (classOf[IllegalArgumentException], "bad argument #"<<"1">>" to 'format' (number has no integer representation)")

      program ("""return ("%i"):format(42)""") succeedsWith ("42")
      program ("""return ("%i"):format(-15)""") succeedsWith ("-15")
      program ("""return ("%i"):format(50.0)""") succeedsWith ("50")
      program ("""return ("%i"):format(-2.0)""") succeedsWith ("-2")
      program ("""return ("%i"):format("102")""") succeedsWith ("102")
      program ("""return ("%i"):format("234.00")""") succeedsWith ("234")

      program ("""return ("%o"):format(1234)""") succeedsWith ("2322")
      program ("""return ("%o"):format(-1)""") succeedsWith ("1777777777777777777777")
      program ("""return ("%o"):format(45.0)""") succeedsWith ("55")
      program ("""return ("%o"):format("321")""") succeedsWith ("501")
      program ("""return ("%o"):format("234.00")""") succeedsWith ("352")

      program ("""return ("%u"):format(1234)""") succeedsWith ("1234")
      program ("""return ("%u"):format((2<<63) - 1)""") succeedsWith ("18446744073709551615")
      program ("""return ("%u"):format(-1)""") succeedsWith ("18446744073709551615")
      program ("""return ("%u"):format(45.0)""") succeedsWith ("45")
      program ("""return ("%u"):format("321")""") succeedsWith ("321")
      program ("""return ("%u"):format("-234.00")""") succeedsWith ("18446744073709551382")

      program ("""return ("%x:%X"):format(1234,1234)""") succeedsWith ("4d2:4D2")
      program ("""return ("%x:%X"):format((2<<63)-1,(2<<63)-1)""") succeedsWith ("ffffffffffffffff:FFFFFFFFFFFFFFFF")
      program ("""return ("%x:%X"):format(-1,-1)""") succeedsWith ("ffffffffffffffff:FFFFFFFFFFFFFFFF")
      program ("""return ("%x:%X"):format(45.0,45.0)""") succeedsWith ("2d:2D")
      program ("""return ("%x:%X"):format("381","381")""") succeedsWith ("17d:17D")
      program ("""return ("%#x:%#X"):format("381","381")""") succeedsWith ("0x17d:0X17D")
      program ("""return ("%x:%X"):format("-234.00","-234.00")""") succeedsWith ("ffffffffffffff16:FFFFFFFFFFFFFF16")

      program ("""return string.format('%5.2s', "hello")""") succeedsWith ("   he")
      program ("""return string.format('%-5.2s', "hello")""") succeedsWith ("he   ")

      program ("""return string.format("%+f%+f", 1/0, -1/0)""") succeedsWith ("+inf-inf")

      program ("""return string.format("%-+#13.f", 0/0)""") succeedsWith ("nan          ")
      program ("""return string.format("%0.3f", 1.2345)""") succeedsWith ("1.235")
      program ("""return string.format('%06.2f', 1/2)""") succeedsWith ("000.50")
      program ("""return string.format('%0+6.2f', 1/2)""") succeedsWith ("+00.50")

      program ("""return string.format("%+10.2e", 5/7)""") succeedsWith (" +7.14e-01")
      program ("""return string.format("%.2e", 500/11)""") succeedsWith ("4.55e+01")
      program ("""return string.format("% .3E", 3/4)""") succeedsWith (" 7.500E-01")

      program ("""return string.format("%.5a", 5/11)""") succeedsWith ("0x1.d1746p-2")
      program ("""return string.format("%0.3A", 11/6)""") succeedsWith ("0X1.D55P+0")

      program ("""return string.format("%g", 3/7000)""") succeedsWith ("0.000428571")
      program ("""return string.format("%g", 3/70000)""") succeedsWith ("4.28571e-05")

      program ("""return ("%c%c%c%c%c"):format(104,101,108,108,111)""") succeedsWith ("hello")
    }

    about ("find") {

      program ("""return string.find("hello there", "%a+")""") succeedsWith (1, 5)
      program ("""return string.find("hello there", "%a+", -3)""") succeedsWith (9, 11)

      program ("""return string.find("hello there", "(%a+)")""") succeedsWith (1, 5, "hello")
      program ("""return string.find("hello there", "(%a+)", 5)""") succeedsWith (5, 5, "o")
      program ("""return string.find("hello there", "(%a+)", 6)""") succeedsWith (7, 11, "there")

      program ("""return string.find("hello there", "", 5)""") succeedsWith (5, 4)
      program ("""return string.find("hello there", "()", 5)""") succeedsWith (5, 4, 5)
      program ("""return string.find("hello there", "", 5, true)""") succeedsWith (5, 4)

      program ("""return string.find("hello there", "e", 1, true)""") succeedsWith (2, 2)
      program ("""return string.find("hello there", "e", 0, true)""") succeedsWith (2, 2)
      program ("""return string.find("hello there", "e", -1, true)""") succeedsWith (11, 11)
      program ("""return string.find("hello there", "e", -5, true)""") succeedsWith (9, 9)
      program ("""return string.find("hello there", "e", -20, true)""") succeedsWith (2, 2)

      program ("""return string.find("hello there", "he", -3, true)""") succeedsWith (null)
      program ("""return string.find("hello there", "he", -4, true)""") succeedsWith (8, 9)

      program ("""return string.find("hello there", "()(.)%2")""") succeedsWith (3, 4, 3, "l")

    }

    about ("match") {

      program ("""return string.match("hello there", "he")""") succeedsWith ("he")
      program ("""return string.match("hello there", "()he")""") succeedsWith (1)
      program ("""return string.match("hello there", "()(he)")""") succeedsWith (1, "he")

      program ("""return string.match("hello there", "")""") succeedsWith ("")
      program ("""return string.match("hello there", "", 0)""") succeedsWith ("")
      program ("""return string.match("hello there", "", -1)""") succeedsWith ("")
      program ("""return string.match("hello there", "", -20)""") succeedsWith ("")
      program ("""return string.match("hello there", "", 20)""") succeedsWith (null)

      program ("""return string.match("hello there", "()", 20)""") succeedsWith (null)
      program ("""return string.match("hello there", "()", -20)""") succeedsWith (1)

      program ("""return string.match("hello there", "()(.)%2")""") succeedsWith (3, "l")

      program ("""return string.match("hello there", "()(%a+)")""") succeedsWith (1, "hello")
      program ("""return string.match("hello there", "()(%a+)", 2)""") succeedsWith (2, "ello")
      program ("""return string.match("hello there", "()(%a+)", -2)""") succeedsWith (10, "re")

      program ("""return string.match("hello there", "()((%a).+%3)")""") succeedsWith (1, "hello th", "h")
      program ("""return string.match("hello there", "()((%a).+%3)", 0)""") succeedsWith (1, "hello th", "h")
      program ("""return string.match("hello there", "()((%a).+%3)", 2)""") succeedsWith (2, "ello there", "e")
      program ("""return string.match("hello there", "()((%a).+%3)", -3)""") succeedsWith (9, "ere", "e")


    }

    // will need table.unpack for this
    in (FullContext) {

      about ("gmatch") {

        def gmatches(name: String, s: String, pattern: String): RichFragment.InContext = {
          val namePrefix = "-- " + name
          val fn =
            """local function gmatches(s, pattern)
              |  local t, i = {}, 1
              |  for m in string.gmatch(s, pattern) do
              |    t[i] = m
              |    i = i + 1
              |  end
              |  return table.unpack(t)
              |end
            """
          program (namePrefix + "\n" + fn.stripMargin + "\n" + "return gmatches("
              + LuaFormat.escape(s) + ", " + LuaFormat.escape(pattern) + ")\n")
        }

        gmatches ("1", "hello world", "%a+") succeedsWith ("hello", "world")
        gmatches ("2", "hello world", "(%a+)") succeedsWith ("hello", "world")
        gmatches ("3", "!! hello--world !!", "(%a+)") succeedsWith ("hello", "world")

        gmatches ("4", "hi.", "()") succeedsWith (1, 2, 3, 4)
        gmatches ("5", "hello world", "()%a+") succeedsWith (1, 7)

      }

      about ("gsub") {

        program ("""return string.gsub()""") failsWith "bad argument #1 to 'gsub' (string expected, got no value)"
        program ("""return string.gsub("")""") failsWith "bad argument #2 to 'gsub' (string expected, got no value)"
        program ("""return string.gsub("", "")""") failsWith "bad argument #3 to 'gsub' (string/function/table expected)"

        program ("""return string.gsub("", "", "")""") succeedsWith ("", 1)
        program ("""return string.gsub("", "", "", 0)""") succeedsWith ("", 0)
        program ("""return string.gsub("", "", "", -1)""") succeedsWith ("", -1)

        program ("""return string.gsub("", "", "", {})""") failsWith "bad argument #4 to 'gsub' (number expected, got table)"
        program ("""return string.gsub("", "", "", 2.1)""") failsWith "bad argument #4 to 'gsub' (number has no integer representation)"

        program ("""return string.gsub("", "", 3)""") succeedsWith ("3", 1)

        // examples from the manual

        program (
          """return string.gsub("hello world", "(%w+)", "%1 %1")
          """
        ) succeedsWith ("hello hello world world", 2)

        program (
          """return string.gsub("hello world", "%w+", "%0 %0", 1)
          """
        ) succeedsWith ("hello hello world", 1)

        program (
          """return string.gsub("hello world from Lua", "(%w+)%s*(%w+)", "%2 %1")
          """
        ) succeedsWith ("world hello Lua from", 2)

        program (
          """local function getenv(n)
            |  if n == "HOME" then return "/home/roberto"
            |  elseif n == "USER" then return "roberto"
            |  else return nil
            |  end
            |end
            |return string.gsub("home = $HOME, user = $USER", "%$(%w+)", getenv)
          """
        ) succeedsWith ("home = /home/roberto, user = roberto", 2)

        program (
          """return string.gsub("4+5 = $return 4+5$", "%$(.-)%$", function (s)
            |  return load(s)()
            |end)
          """
        ) succeedsWith ("4+5 = 9", 1)

        program (
          """local t = {name="lua", version="5.3"}
            |return string.gsub("$name-$version.tar.gz", "%$(%w+)", t)
          """
        ) succeedsWith ("lua-5.3.tar.gz", 2)

      }

    }

  }

}
