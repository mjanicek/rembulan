package net.sandius.rembulan.compiler.gen

import net.sandius.rembulan.test.{DebugLibFragments, FragmentExecTestSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DebugFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(DebugLibFragments)
  override def expectations = Seq(DebugLibFragments)
  override def contexts = Seq(Debug, Full)

  override def steps = Seq(1, Int.MaxValue)

}