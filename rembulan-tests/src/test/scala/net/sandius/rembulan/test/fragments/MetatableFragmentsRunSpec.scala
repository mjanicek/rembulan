package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.FragmentExecTestSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MetatableFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(MetatableFragments)
  override def expectations = Seq(MetatableFragments)
  override def contexts = Seq(Basic)

  override def steps = Seq(1, Int.MaxValue)

}
