package net.sandius.rembulan.compiler.gen

import net.sandius.rembulan.test.{FragmentExecTestSuite, MetatableFragments}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MetatableFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(MetatableFragments)
  override def expectations = Seq(MetatableFragments)
  override def contexts = Seq(Basic)

  override def steps = Seq(1, Int.MaxValue)

}
