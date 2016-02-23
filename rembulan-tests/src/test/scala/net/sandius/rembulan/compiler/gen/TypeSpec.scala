package net.sandius.rembulan.compiler.gen

import net.sandius.rembulan.compiler.types.{FunctionType, GradualTypeLike, TypeSeq, Type}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{MustMatchers, FunSpec}

@RunWith(classOf[JUnitRunner])
class TypeSpec extends FunSpec with MustMatchers {

  import LuaTypes._

  def consistency[T <: GradualTypeLike[T]](l: T, r: T, expected: Boolean) {

    it(l + " is " + (if (!expected) "NOT " else "") + "consistent with " + r) {
      if (l != r) {
        val lr = l isConsistentWith r
        val rl = r isConsistentWith l

        lr mustEqual rl  // symmetry
        lr mustBe expected  // correct result
      }
      else {
        (l isConsistentWith r) mustBe expected
      }
    }

  }

  def subtype(l: Type, r: Type, expected: Boolean) {
    it(l + " is " + (if (!expected) "NOT " else "") + "a subtype of " + r) {
      l.isSubtypeOf(r) mustBe expected
    }
  }

  def consistentSubtype[T <: GradualTypeLike[T]](l: T, r: T, expected: Boolean) {
    it(l + " is " + (if (!expected) "NOT " else "") + "a consistent subtype of " + r) {
      l.isConsistentSubtypeOf(r) mustBe expected
    }
  }

  trait Gradual[T <: GradualTypeLike[T]] {

    protected def self: T

    def ~(r: T) = consistency(self, r, true)
    def !~(r: T) = consistency(self, r, false)

    def ~<(r: T) = consistentSubtype(self, r, true)
    def !~<(r: T) = consistentSubtype(self, r, false)

  }

  trait Subtypable {

    protected def self: Type

    def =<(r: Type) = subtype(self, r, true)
    def !=<(r: Type) = subtype(self, r, false)

  }

  implicit class RichType(val self: Type) extends Gradual[Type] with Subtypable

  implicit class RichTypeSeq(val self: TypeSeq) extends Gradual[TypeSeq]

  describe ("consistency:") {

    describe ("simple type") {

      ANY ~ DYNAMIC
      ANY !~ NIL

      NUMBER !~ NUMBER_INTEGER

      NUMBER ~ NUMBER
      NUMBER_INTEGER ~ NUMBER_INTEGER

      NUMBER !~ NUMBER_FLOAT
      NUMBER !~ STRING

      NUMBER ~ DYNAMIC
      NUMBER_INTEGER !~ NIL

      FunctionType.of(TypeSeq.of(DYNAMIC, DYNAMIC), TypeSeq.of(BOOLEAN, ANY)) ~ FunctionType.of(TypeSeq.of(DYNAMIC, DYNAMIC), TypeSeq.of(DYNAMIC, ANY))

    }

    describe ("type sequence") {

      TypeSeq.empty() ~ TypeSeq.empty()
      TypeSeq.empty() !~ TypeSeq.vararg()
      TypeSeq.vararg() ~ TypeSeq.vararg()

      TypeSeq.of(NUMBER) !~ TypeSeq.of(NUMBER_FLOAT, ANY)
      TypeSeq.of(NUMBER_FLOAT) !~ TypeSeq.of(NUMBER, ANY)

      TypeSeq.of(DYNAMIC, DYNAMIC) ~ TypeSeq.of(BOOLEAN, STRING)

      TypeSeq.of(DYNAMIC, NUMBER_INTEGER) ~ TypeSeq.of(NUMBER_FLOAT, DYNAMIC)

    }

  }

  describe ("subtyping:") {

    describe ("simple type") {

      ANY !=< DYNAMIC
      DYNAMIC !=< ANY

      NIL =< ANY
      NIL !=< DYNAMIC

      NUMBER =< NUMBER

      NUMBER_INTEGER =< NUMBER
      NUMBER_FLOAT =< NUMBER

      NUMBER_INTEGER !=< NUMBER_FLOAT
      NUMBER_FLOAT !=< NUMBER_INTEGER

    }

  }

  describe ("consistent subtyping:") {

    describe ("simple type") {

      ANY ~< DYNAMIC
      DYNAMIC ~< ANY

      NIL ~< DYNAMIC
      DYNAMIC ~< NIL

    }

    describe ("type sequence") {

      TypeSeq.empty() ~< TypeSeq.empty()
      TypeSeq.empty() ~< TypeSeq.vararg()
      TypeSeq.vararg() ~< TypeSeq.vararg()
      TypeSeq.vararg() !~< TypeSeq.empty()

      TypeSeq.of(NUMBER) !~< TypeSeq.of(NUMBER_FLOAT, ANY)
      TypeSeq.of(NUMBER_FLOAT) ~< TypeSeq.of(NUMBER, ANY)

      TypeSeq.of(DYNAMIC, DYNAMIC) ~< TypeSeq.of(BOOLEAN, STRING)
      TypeSeq.of(BOOLEAN, STRING) ~< TypeSeq.of(DYNAMIC, DYNAMIC)

      TypeSeq.of(DYNAMIC, NUMBER_INTEGER) ~< TypeSeq.of(NUMBER_FLOAT, DYNAMIC)
      TypeSeq.of(NUMBER_FLOAT, DYNAMIC) ~< TypeSeq.of(DYNAMIC, NUMBER_INTEGER)

    }

  }


}
