package net.sandius.rembulan.compiler.gen

import net.sandius.rembulan.test.{FragmentExecTestSuite, StringFragments}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StringFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(StringFragments)
  override def expectations = Seq(StringFragments)
  override def contexts = Seq(Str)

  override def steps = Seq(1, Int.MaxValue)

}

