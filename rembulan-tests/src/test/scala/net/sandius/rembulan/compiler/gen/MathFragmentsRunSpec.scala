package net.sandius.rembulan.compiler.gen

import net.sandius.rembulan.test.{FragmentExecTestSuite, MathFragments}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MathFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(MathFragments)
  override def expectations = Seq(MathFragments)
  override def contexts = Seq(Math)

  override def steps = Seq(1, Int.MaxValue)

}

