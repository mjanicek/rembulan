package net.sandius.rembulan.parser

import java.io.ByteArrayInputStream

import net.sandius.rembulan.test._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class FragmentParsingTest extends FunSpec with MustMatchers {

  val bundles = Seq(
    BasicFragments,
    BasicLibFragments,
    CoroutineFragments,
    DebugLibFragments,
    IOLibFragments,
    MathFragments,
    MetatableFragments,
    OperatorFragments,
    StringFragments,
    TableLibFragments
  )

  for (b <- bundles) {
    describe ("from " + b.name + " :") {
      for (f <- b.all) {
        describe (f.description) {
          it ("can be parsed") {
            val code = f.code

            println("--BEGIN--")
            println(code)
            println("---END---")

            val bais = new ByteArrayInputStream(code.getBytes)
            val chunk = new Parser(bais).Chunk()
            chunk mustNot be (null)
          }
        }
      }
    }
  }


}
