package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.FragmentExecTestSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OperatorFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(OperatorFragments)
  override def expectations = Seq(OperatorFragments)
  override def contexts = Seq(Empty)

  override def steps = Seq(1, Int.MaxValue)

}
