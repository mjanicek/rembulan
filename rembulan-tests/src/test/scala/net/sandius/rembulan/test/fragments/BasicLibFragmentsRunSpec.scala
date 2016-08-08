package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.FragmentExecTestSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BasicLibFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(BasicLibFragments)
  override def expectations = Seq(BasicLibFragments)
  override def contexts = Seq(Basic)

  override def steps = Seq(1, Int.MaxValue)

}
