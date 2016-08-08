package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.FragmentExecTestSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DebugLibFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(DebugLibFragments)
  override def expectations = Seq(DebugLibFragments)
  override def contexts = Seq(Debug, Full)

  override def steps = Seq(1, Int.MaxValue)

}