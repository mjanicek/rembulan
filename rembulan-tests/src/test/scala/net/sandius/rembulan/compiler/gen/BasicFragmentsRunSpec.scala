package net.sandius.rembulan.compiler.gen

import net.sandius.rembulan.test.{BasicFragments, FragmentExecTestSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BasicFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(BasicFragments)
  override def expectations = Seq(BasicFragments)
  override def contexts = Seq(Empty, Basic)

  override val step = 1

}
