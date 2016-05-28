package net.sandius.rembulan.test

import net.sandius.rembulan.core.LuaRuntimeException
import org.scalatest.FunSpec

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.implicitConversions

trait Fragment {

  def description: String

  private var _code: String = null

  def code: String = _code

  protected def code_=(v: String): Unit = {
    require (v != null)
    this._code = v.stripMargin
  }

}

object Fragment {

  class DefaultImpl(_desc: String, _code: String) extends Fragment {
    require (_desc != null)
    require (_code != null)

    override val description = _desc
    override val code = _code.stripMargin
  }

  def apply(description: String, code: String): Fragment = new DefaultImpl(description, code)

}

trait FragmentBundle {

  implicit protected val bundle = this

  private val fragments = ArrayBuffer.empty[Fragment]

  protected def register(fragment: Fragment): Fragment = {
    fragments.append(fragment)
    fragment
  }

  def lookup(name: String): Option[Fragment] = fragments find { _.description == name }

  def all: Iterable[Fragment] = fragments

  protected def fragment(name: String)(body: String): Fragment = {
    register(Fragment(name, body))
  }

}

trait FragmentExpectations {

  import FragmentExpectations._

  protected val EmptyContext = Env.Empty
  protected val BasicContext = Env.Basic
  protected val CoroContext = Env.Coro
  protected val MathContext = Env.Math
  protected val StringContext = Env.Str

  private val expectations = mutable.Map.empty[Fragment, mutable.Map[Env, Expect]]

  def expectationFor(fragment: Fragment): Option[Map[Env, Expect]] = {
    expectations.get(fragment) map { _.toMap }
  }

  private def addExpectation(fragment: Fragment, ctx: Env, expect: Expect): Unit = {
    val es = expectations.getOrElseUpdate(fragment, mutable.Map.empty)
    es(ctx) = expect
  }

  protected class RichFragment(fragment: Fragment) {
    def in(ctx: Env) = new RichFragment.InContext(fragment, ctx)
  }

  protected object RichFragment {

    class InContext(fragment: Fragment, ctx: Env) {
      def succeedsWith(values: Any*) = {
        addExpectation(fragment, ctx, Expect.Success(values map toRembulanValue))
      }
      def failsWith(clazz: Class[_ <: Throwable]) = {
        addExpectation(fragment, ctx, Expect.Failure.ExceptionFailure(Some(clazz), None))
      }
      def failsWith(clazz: Class[_ <: Throwable], message: String) = {
        addExpectation(fragment, ctx, Expect.Failure.ExceptionFailure(Some(clazz), Some(message)))
      }
      def failsWith(message: String) = {
        addExpectation(fragment, ctx, Expect.Failure.ExceptionFailure(None, Some(message)))
      }
      def failsWithLuaError(errorObject: AnyRef) = {
        addExpectation(fragment, ctx, Expect.Failure.LuaErrorFailure(errorObject))
      }
    }

  }

  protected implicit def fragmentToRichFragment(frag: Fragment): RichFragment = new RichFragment(frag)

  // for code structuring purposes only
  protected def expect(body: => Unit): Unit = {
    body
  }

  protected def stringStartingWith(prefix: String) = ValueMatch.StringStartingWith(prefix)

}

object FragmentExpectations {

  sealed trait Env
  object Env {
    case object Empty extends Env
    case object Basic extends Env
    case object Coro extends Env
    case object Math extends Env
    case object Str extends Env
  }

  sealed trait Expect {
    def tryMatch(actual: Either[Throwable, Seq[AnyRef]])(spec: FunSpec): Unit
  }

  object Expect {
    case class Success(vms: Seq[ValueMatch]) extends Expect {
      override def tryMatch(actual: Either[Throwable, Seq[AnyRef]])(spec: FunSpec) = {
        actual match {
          case Right(vs) =>
            if (vs.size != vms.size) {
              spec.fail("result list size does not match: expected " + vms.size + ", got " + vs.size)
            }
            spec.assertResult(vs.size)(vms.size)

            for (((v, vm), i) <- (vs zip vms).zipWithIndex) {
              if (!vm.matches(v)) {
                spec.fail("value #" + i + " does not match: expected " + vm + ", got " + v)
              }
            }

          case Left(ex) =>
            spec.fail("Expected success, got an exception: " + ex.getMessage, ex)
        }
      }
    }

    sealed trait Failure extends Expect {

      protected def matchError(ex: Throwable)(spec: FunSpec): Unit

      override def tryMatch(actual: Either[Throwable, Seq[AnyRef]])(spec: FunSpec) = {
        actual match {
          case Right(vs) => spec.fail("Expected failure, got success")
          case Left(ex) => matchError(ex)(spec)
        }
      }

    }

    object Failure {

      case class ExceptionFailure(optExpectClass: Option[Class[_ <: Throwable]], optExpectMessage: Option[String]) extends Failure {
        override protected def matchError(ex: Throwable)(spec: FunSpec) = {
          for (expectClass <- optExpectClass) {
            val actualClass = ex.getClass
            if (!expectClass.isAssignableFrom(actualClass)) {
              spec.fail("Expected exception of type " + expectClass.getName + ", got " + actualClass.getName)
            }
          }

          for (expectMessage <- optExpectMessage) {
            val actualMessage = ex.getMessage
            if (actualMessage != expectMessage) {
              spec.fail("Error message mismatch: expected \"" + expectMessage + "\", got \"" + actualMessage + "\"")
            }
          }
        }
      }

      case class LuaErrorFailure(expectErrorObject: AnyRef) extends Failure {
        override protected def matchError(ex: Throwable)(spec: FunSpec) = {
          ex match {
            case le: LuaRuntimeException =>
              val actualErrorObject = le.getErrorObject
              if (expectErrorObject != actualErrorObject) {
                spec.fail("Error object mismatch: expected [" + expectErrorObject + "], got [" + actualErrorObject + "]")
              }
          }
        }
      }

    }

  }

  sealed trait ValueMatch {
    def matches(o: AnyRef): Boolean
  }
  object ValueMatch {
    case class Eq(v: AnyRef) extends ValueMatch {
      override def matches(o: AnyRef) = {
        if (o == null || v == null) {
          o eq v
        }
        else {
          v.getClass == o.getClass && v == o
        }
      }
    }
    case class SubtypeOf(c: Class[_]) extends ValueMatch {
      override def matches(o: AnyRef) = if (o == null) false else c.isAssignableFrom(o.getClass)
    }
    case class StringStartingWith(prefix: String) extends ValueMatch {
      override def matches(o: AnyRef) = {
        o match {
          case s: String if s.startsWith(prefix) => true
          case _ => false
        }
      }
    }

  }

  private def toRembulanValue(v: Any): ValueMatch = {
    import ValueMatch._
    v match {
      case vm: ValueMatch => vm
      case null => Eq(null)
      case b: Boolean => Eq(java.lang.Boolean.valueOf(b))
      case i: Int => Eq(java.lang.Long.valueOf(i))
      case l: Long => Eq(java.lang.Long.valueOf(l))
      case f: Float => Eq(java.lang.Double.valueOf(f))
      case d: Double => Eq(java.lang.Double.valueOf(d))
      case s: String => Eq(s)
      case c: Class[_] => SubtypeOf(c)
      case _ => throw new IllegalArgumentException("illegal value: " + v)
    }
  }

}

trait OneLiners { this: FragmentBundle with FragmentExpectations =>

  private var prefixes: List[String] = Nil

  def about(desc: String)(body: => Unit): Unit = {
    val oldPrefixes = prefixes
    try {
      prefixes = desc :: oldPrefixes
      body
    }
    finally {
      prefixes = oldPrefixes
    }
  }

  private var context: FragmentExpectations.Env = null

  def in(env: FragmentExpectations.Env)(body: => Unit): Unit = {
    val oldContext = context
    try {
      context = env
      body
    }
    finally {
      context = oldContext
    }
  }

  protected def thisContext: FragmentExpectations.Env = context

  def program(body: String): RichFragment.InContext = {
    val name = (body :: prefixes).reverse.mkString(": ")
    fragment(name)(body) in context
  }

}