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

  about ("byte") {
    in (StringContext) {

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
      program ("""return ("Boom"):byte(true)""") failsWith (classOf[IllegalArgumentException], "bad argument #1 to 'byte' (number expected, got boolean)")

    }
  }

  about ("char") {
    in (StringContext) {

      program ("""return string.char()""") succeedsWith ("")
      program ("""return string.char(104, 101, 108, 108, 111)""") succeedsWith ("hello")

      program ("""return string.char("104", "105.0", 33.0)""") succeedsWith ("hi!")

      program ("""string.char(-1)""") failsWith (classOf[IllegalArgumentException], "bad argument #1 to 'char' (value out of range)")
      program ("""string.char(256)""") failsWith (classOf[IllegalArgumentException], "bad argument #1 to 'char' (value out of range)")

    }
  }

}
