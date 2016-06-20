package net.sandius.rembulan.parser

import scala.collection.mutable.ArrayBuffer

object Expressions {

  private val exprs = ArrayBuffer.empty[(String, Boolean)]

  def get: Seq[(String, Boolean)] = {
    exprs
  }

  protected def ok(s: String): Unit = {
    exprs += ((s, true))
  }

  protected def nok(s: String): Unit = {
    exprs += ((s, false))
  }

  ok ("1")
  ok ("true")
  ok ("nil")
  ok ("x")

  nok ("x x")

  ok ("1 + 2")
  ok ("x ^ 2")
  ok ("-x ^ y")
  ok ("-2 ^ -2")

  ok ("1 + 2 + 3")
  ok ("1 ^ 2 ^ 3")
  ok ("1 .. 2 .. 3")

  ok ("-1 ^ -2 .. -3 + 10")

  ok ("...")
  ok ("(...)")
  ok ("... << 2")

  ok ("function () end")
  ok ("function (x) return x * x end")

  ok ("x.y")
  ok ("x.y.z")
  ok ("x[1 / 0]")

  ok ("{}")
  ok ("{x}")
  ok ("{x = y}")
  ok ("{x;}")
  ok ("{2,1,}")
  ok ("{1, 2, 3, 4, 5}")
  ok ("{1, 2, 3, 4, 5,}")

  ok ("{f()}")
  ok ("{(f())}")
  ok ("{f(),'boo'}")

  ok ("{...}")
  ok ("{x = ...}")

  nok ("{;}")

  ok ("#t")

  ok ("(4).x")
  ok ("(4)()")

  ok ("f() + 1")
  ok ("f(...) + 1")

  ok ("(...).x + 1")

  nok ("4.x")
  nok ("0()")
  nok ("nil.x")
  nok ("true()")

  ok ("f:g()")

  ok ("f(f(f))")

  ok ("f()")
  ok ("f.x()")

  ok ("f {}")
  ok ("f \"x\"")

  ok ("f.x()[1]")
  ok ("f.x().y()")

  ok ("f() * 1 << 2")

  ok ("f(...)")

  ok ("x or y > 0")
  ok ("not not x == false and x ~= nil")

}
