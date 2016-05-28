package net.sandius.rembulan.test

import net.sandius.rembulan.core._
import net.sandius.rembulan.{core => lua}

object StringFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  val StringSetsMetatable = fragment ("StringSetsMetatable") {
    """return getmetatable("hello")
    """
  }
  StringSetsMetatable in StringContext succeedsWith (classOf[Table])

  val StringTableIsReferencedInStringMetatable = fragment ("StringTableIsReferencedInStringMetatable") {
    """local mt = getmetatable("hello")
      |return mt == string, mt.__index == string
    """
  }
  StringTableIsReferencedInStringMetatable in StringContext succeedsWith (false, true)

  val LenMethodIsEqualToLenOperator = fragment ("LenMethodIsEqualToLenOperator") {
    """local hello = "hello"
      |return hello:len() == #hello
    """
  }
  LenMethodIsEqualToLenOperator in StringContext succeedsWith (true)

  in (StringContext) {

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

      program ("""return ("%d"):format()""") failsWith (classOf[IllegalArgumentException], "bad argument #"<<"1">>" to 'format' (no value)")
      program ("""return ("%d"):format("hi")""") failsWith (classOf[IllegalArgumentException], "bad argument #"<<"1">>" to 'format' (number expected, got string)")
      program ("""return ("%d"):format("1")""") succeedsWith ("1")

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
      program ("""return ("%x:%X"):format("-234.00","-234.00")""") succeedsWith ("ffffffffffffff16:FFFFFFFFFFFFFF16")

      program ("""return ("%c%c%c%c%c"):format(104,101,108,108,111)""") succeedsWith ("hello")
    }

  }

}
