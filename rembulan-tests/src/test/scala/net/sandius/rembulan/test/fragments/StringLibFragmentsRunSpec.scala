package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.FragmentExecTestSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StringLibFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(StringLibFragments)
  override def expectations = Seq(StringLibFragments)
  override def contexts = Seq(Str)

  override def steps = Seq(1, Int.MaxValue)

}

