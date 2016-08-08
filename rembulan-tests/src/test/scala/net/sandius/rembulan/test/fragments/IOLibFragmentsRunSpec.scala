package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.FragmentExecTestSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IOLibFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(IOLibFragments)
  override def expectations = Seq(IOLibFragments)
  override def contexts = Seq(IO)

  override def steps = Seq(1, Int.MaxValue)


}
