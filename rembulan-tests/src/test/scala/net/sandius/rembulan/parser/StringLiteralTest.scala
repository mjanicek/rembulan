package net.sandius.rembulan.parser

import net.sandius.rembulan.parser.ast.StringLiteral
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class StringLiteralTest extends FunSpec with MustMatchers {

  describe ("string literal") {

    def go(l: String, expected: String): Unit = {
      describe (l) {
        it ("is parsed correctly") {
          val v = StringLiteral.fromString(l).value()
          v mustEqual expected
        }
      }
    }

    go ("\"hello\"", "hello")
    go ("'there'", "there")

    go ("\"doub'le\"", "doub'le")
    go ("'sing\"le'", "sing\"le")

    go ("\"esc\\n\"", "esc\n")
    go ("\"a\\tb\\r\"", "a\tb\r")
    go ("\"\f\b\"", "\f\b")

    go ("\"\\104\\101\\108\\32\\108\\111\"", "hel lo")

    go ("\"\\x68\\101\\x6c\\x6Co\"", "hello")

    go ("\"\\u{68}\\u{69}", "hi")

  }

}
