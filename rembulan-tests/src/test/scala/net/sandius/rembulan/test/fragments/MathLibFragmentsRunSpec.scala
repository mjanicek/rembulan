package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.FragmentExecTestSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MathLibFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(MathLibFragments)
  override def expectations = Seq(MathLibFragments)
  override def contexts = Seq(Math)

  override def steps = Seq(1, Int.MaxValue)

}

