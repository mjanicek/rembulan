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

  protected def not_ok(s: String): Unit = {
    exprs += ((s, false))
  }

  ok ("1")
  ok ("true")
  ok ("nil")
  ok ("x")

  not_ok ("x x")

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
  ok ("function () return x end")

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

  not_ok ("{;}")

  ok ("#t")

  ok ("(4).x")
  ok ("(4)()")

  ok ("f() + 1")
  ok ("f(...) + 1")

  ok ("(function () return x end)()")
  ok ("(function (x) return x end)(10)")
  ok ("(function (x) return x * x end)(10) + 1")

  ok ("(...).x + 1")

  not_ok ("4.x")
  not_ok ("0()")
  not_ok ("nil.x")
  not_ok ("true()")

  ok ("f:g()")

  ok ("f(f(f))")
  ok ("f(f(f(f)))")

  ok ("f()")
  ok ("f.x()")

  ok ("f {}")
  ok ("f \"x\"")

  ok ("f.x()[1]")
  ok ("f.x().y()")

  ok ("f() * 1 << 2")

  ok ("f(...)")

  ok ("not x")
  ok ("not not x")
  ok ("x or y")
  ok ("x and y")
  ok ("x or y or z")

  ok ("x and y or z")

  ok ("x or y > 0")

  ok ("x >= 0 and x <= 10")
  ok ("x < 0 or x > 10")

  ok ("not not x == false and x ~= nil")

  ok ("[[hello]]")
  ok ("[=[hello]] there]=]")
  ok ("[==[hello [[and]=] welcome]== [[ [==[ ] ==] ]==]")

  ok ("[[hello]] .. ' ' .. there")

  ok ("[[[[]=]]")
  ok ("[[[[] ]]")
  not_ok ("[[[[]]]")
  not_ok ("[[[[]=]]]")
  ok ("[[[[]=] ]]")

}
