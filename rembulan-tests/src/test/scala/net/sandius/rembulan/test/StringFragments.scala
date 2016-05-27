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

    }
  }

}
