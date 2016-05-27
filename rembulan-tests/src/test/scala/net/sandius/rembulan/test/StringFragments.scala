package net.sandius.rembulan.test

import net.sandius.rembulan.core._
import net.sandius.rembulan.{core => lua}

object StringFragments extends FragmentBundle with FragmentExpectations {

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

  val ByteMethod = fragment ("ByteMethod") {
    """local s = "hello"
      |return s:byte(1), s:byte(-1), s:byte(2), s:byte(10), s:byte(-#s), s:byte(#s)
    """
  }
  ByteMethod in StringContext succeedsWith (104, 111, 101, null, 104, 111)

  val ByteMethodForZeroIndex = fragment ("ByteMethodForZeroIndex") {
    """return ("hello"):byte(0)
    """
  }
  ByteMethodForZeroIndex in StringContext succeedsWith ()

  val ByteMethodForOutsidePositiveIndex = fragment ("ByteMethodForOutsidePositiveIndex") {
    """return ("hello"):byte(100)
    """
  }
  ByteMethodForOutsidePositiveIndex in StringContext succeedsWith ()

  val ByteMethodForOutsideNegativeIndex = fragment ("ByteMethodForOutsideNegativeIndex") {
    """return ("hello"):byte(-100)
    """
  }
  ByteMethodForOutsideNegativeIndex in StringContext succeedsWith ()

  val ByteInterval = fragment ("ByteInterval") {
    """return ("hello"):byte(1, -1)
    """
  }
  ByteInterval in StringContext succeedsWith (104, 101, 108, 108, 111)

  val ByteOutsideInterval = fragment ("ByteOutsideInterval") {
    """return ("hello"):byte(0, 100)
    """
  }
  ByteOutsideInterval in StringContext succeedsWith (104, 101, 108, 108, 111)

  val ByteWithoutArgs = fragment ("ByteWithoutArgs") {
    """return ("hello"):byte()
    """
  }
  ByteWithoutArgs in StringContext succeedsWith (104)

  val EmptyStringByteWithoutArgs = fragment ("EmptyStringByteWithoutArgs") {
    """return (""):byte()
    """
  }
  EmptyStringByteWithoutArgs in StringContext succeedsWith ()

}
