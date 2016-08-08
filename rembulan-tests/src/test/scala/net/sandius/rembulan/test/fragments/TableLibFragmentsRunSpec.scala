package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.FragmentExecTestSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TableLibFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(TableLibFragments)
  override def expectations = Seq(TableLibFragments)
  override def contexts = Seq(Tab, Full)

  override def steps = Seq(1, Int.MaxValue)

}