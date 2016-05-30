package net.sandius.rembulan.compiler.gen

import net.sandius.rembulan.test.{FragmentExecTestSuite, IOLibFragments}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IOFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(IOLibFragments)
  override def expectations = Seq(IOLibFragments)
  override def contexts = Seq(IO)

  override def steps = Seq(1, Int.MaxValue)


}
