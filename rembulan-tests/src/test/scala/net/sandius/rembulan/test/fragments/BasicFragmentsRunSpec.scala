package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.FragmentExecTestSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BasicFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(BasicFragments)
  override def expectations = Seq(BasicFragments)
  override def contexts = Seq(Empty, Basic)

  override def steps = Seq(1, Int.MaxValue)

  override def compilerConfigs = CompilerConfigs.All

}
