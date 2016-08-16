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

package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.{FragmentBundle, FragmentExpectations, OneLiners}

object MathLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (MathContext) {

    about ("floor") {
      program ("return math.floor(3.0), math.floor(3.1)") succeedsWith (3, 3)
    }

    about ("modf") {
      program ("return math.modf(3.5)") succeedsWith (3, 0.5)
      program ("return math.modf(-2.5)") succeedsWith (-2, -0.5)
      program ("return math.modf(-3e23)") succeedsWith (-3e23, 0.0)
      program ("return math.modf(3e35)") succeedsWith (3e35, 0.0)
      program ("return math.modf(-1/0)") succeedsWith (Double.NegativeInfinity, 0.0)
      program ("return math.modf(1/0)") succeedsWith (Double.PositiveInfinity, 0.0)
      program ("return math.modf(0/0)") succeedsWith (NaN, NaN)
      program ("return math.modf(3)") succeedsWith (3, 0.0)
      program ("return math.modf(math.mininteger)") succeedsWith (Long.MinValue, 0.0)
    }

    about ("huge") {
      program ("return math.huge") succeedsWith Double.PositiveInfinity
    }

    about ("maxinteger") {
      program ("return math.maxinteger") succeedsWith Long.MaxValue
    }

    about ("mininteger") {
      program ("return math.mininteger") succeedsWith Long.MinValue
    }

    about ("random") {
      program ("math.random(0)") failsWith (classOf[IllegalArgumentException], "bad argument #1 to 'random' (interval is empty)")
      program ("math.random(1, 0)") failsWith (classOf[IllegalArgumentException], "bad argument #1 to 'random' (interval is empty)")

      program ("return math.random(0, 0)") succeedsWith (0)

      program ("return math.random(-10000000000000000000, 10000000000000000000)") failsWith (classOf[IllegalArgumentException], "bad argument #1 to 'random' (number has no integer representation)")

      program ("math.random(1 << 63, (1 << 63) - 1)") failsWith (classOf[IllegalArgumentException], "bad argument #1 to 'random' (interval too large)")
    }

    about ("randomseed") {
      program ("math.randomseed(1)") succeedsWith ()
      program ("math.randomseed(1.0)") succeedsWith ()
      program ("""math.randomseed("1")""") succeedsWith ()

      program ("math.randomseed()") failsWith (classOf[IllegalArgumentException], "bad argument #1 to 'randomseed' (number expected, got no value)")

      program (
        """math.randomseed(0)
          |local x = math.random()
          |math.randomseed(0)
          |local y = math.random()
          |return x == y
        """) succeedsWith (true)
    }

  }

}
