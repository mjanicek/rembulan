/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.compiler.analysis.types

import net.sandius.rembulan.util.ReadOnlyArray
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

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

  implicit class RichTypeSeq(val self: TypeSeq) extends Gradual[TypeSeq] {
    def + = self.withVararg()
    def ->(that: TypeSeq) = LuaTypes.functionType(self, that)
  }

  object T {
    def apply(ts: Type*): TypeSeq = TypeSeq.of(ReadOnlyArray.wrap(ts.toArray), false)
  }

  def equivalent(l: Type, r: Type) {
    l ~ r
    l ~< r
    r ~< l
  }

  def strict_lt(l: Type, r: Type) {
    l !~ r
    l ~< r
    r !~< l
  }

  def not_comparable(l: Type, r: Type) {
    l !~ r
    l !~< r
    r !~< l
  }

  def union(l: Type, r: Type, expected: Type) {
    describe ("union of " + l + " and " + r) {

      if (l == r) {
        val t = l unionWith  r

        it ("exists") {
          t must not be null
        }

        describe ("correctness:") {
          l ~< t
          r ~< t
        }

        it ("is equal to " + expected) {
          t mustEqual expected
        }

      }
      else {
        val lr = l unionWith r
        val rl = r unionWith l

        it ("exists") {
          lr must not be null
          rl must not be null
        }

        it ("is symmetric") {
          lr mustEqual rl
          rl mustEqual lr
        }

        val t = lr

        describe ("correctness:") {
          l ~< t
          r ~< t
        }

        it ("is equal to " + expected) {
          t mustEqual expected
        }

      }
    }
  }

  val D_D = T(DYNAMIC) -> T(DYNAMIC)
  val DD_D = T(DYNAMIC, DYNAMIC) -> T(DYNAMIC)
  val Dv_D = T(DYNAMIC).+ -> T(DYNAMIC)
  val A_A = T(ANY) -> T(ANY)
  val DDv_D = T(DYNAMIC, DYNAMIC).+ -> T(DYNAMIC)
  val NNv_N = T(NUMBER, NUMBER).+ -> T(NUMBER)

  val v_A = T().+ -> T(ANY)
  val v_v = T().+ -> T().+
  val AA_A = T(ANY, ANY) -> T(ANY)
  val NN_N = T(NUMBER, NUMBER) -> T(NUMBER)
  val NN_A = T(NUMBER, NUMBER) -> T(ANY)
  val ii_i = T(NUMBER_INTEGER, NUMBER_INTEGER) -> T(NUMBER_INTEGER)
  val ff_f = T(NUMBER_FLOAT, NUMBER_FLOAT) -> T(NUMBER_FLOAT)

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

      (T(DYNAMIC, DYNAMIC) -> T(BOOLEAN, ANY)) ~ (T(DYNAMIC, DYNAMIC) -> T(DYNAMIC, ANY))

      (T(DYNAMIC) -> T(NUMBER)) ~ (T(STRING) -> T(DYNAMIC))
      (T(DYNAMIC, DYNAMIC).+ -> T(DYNAMIC)) ~ (T(NUMBER, NUMBER).+ -> T(NUMBER))

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

      (T(NUMBER_INTEGER) -> T(DYNAMIC)) ~< (T(DYNAMIC) -> T(NUMBER_INTEGER))

      (T(DYNAMIC, DYNAMIC).+ -> T(DYNAMIC)) ~< (T(NUMBER, NUMBER) -> T(NUMBER))
      (T(NUMBER, NUMBER) -> T(NUMBER)) !~< (T(DYNAMIC, DYNAMIC).+ -> T(DYNAMIC))

      strict_lt(v_A, v_v)
      strict_lt(v_A, A_A)
      not_comparable(v_v, A_A)

      strict_lt(D_D, ANY)
      equivalent(NNv_N, DDv_D)

      strict_lt(v_A, AA_A)
      strict_lt(AA_A, NN_A)
      strict_lt(NN_N, NN_A)
      not_comparable(AA_A, NN_N)

      equivalent(Dv_D, v_A)
      strict_lt(Dv_D, D_D)
      strict_lt(v_A, D_D)

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

  describe ("type union:") {

    union(ANY, DYNAMIC, DYNAMIC)
    union(NIL, DYNAMIC, DYNAMIC)
    union(ANY, BOOLEAN, ANY)
    union(NUMBER, NUMBER_INTEGER, NUMBER)
    union(NUMBER_FLOAT, NUMBER_INTEGER, NUMBER)

    union(AA_A, D_D, T(DYNAMIC, NIL) -> T(DYNAMIC))
    union(DD_D, D_D, DD_D)
    union(DDv_D, D_D, DD_D)

    union(T(NUMBER_INTEGER, NUMBER_FLOAT) -> T(BOOLEAN, STRING), T().+ -> T().+, T(NUMBER_INTEGER, NUMBER_FLOAT) -> T(ANY, ANY).+)

  }


}
