package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.FragmentExecTestSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CoroutineLibFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(CoroutineLibFragments)
  override def expectations = Seq(CoroutineLibFragments)
  override def contexts = Seq(Coro)

  override def steps = Seq(1, Int.MaxValue)

}
