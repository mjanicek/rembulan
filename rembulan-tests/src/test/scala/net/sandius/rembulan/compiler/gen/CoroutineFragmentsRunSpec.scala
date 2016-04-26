package net.sandius.rembulan.compiler.gen

import net.sandius.rembulan.test.{CoroutineFragments, FragmentExecTestSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CoroutineFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(CoroutineFragments)
  override def expectations = Seq(CoroutineFragments)
  override def contexts = Seq(Coro)

  override def steps = Seq(1, Int.MaxValue)

}
