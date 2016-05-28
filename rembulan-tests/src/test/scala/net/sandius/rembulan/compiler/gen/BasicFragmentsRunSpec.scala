package net.sandius.rembulan.compiler.gen

import net.sandius.rembulan.test.{BasicFragments, BasicLibFragments, FragmentExecTestSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BasicFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(BasicFragments, BasicLibFragments)
  override def expectations = Seq(BasicFragments, BasicLibFragments)
  override def contexts = Seq(Empty, Basic)

  override def steps = Seq(1, Int.MaxValue)

}
