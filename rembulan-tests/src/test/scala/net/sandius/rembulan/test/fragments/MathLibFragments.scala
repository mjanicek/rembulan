package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.{FragmentBundle, FragmentExpectations, OneLiners}

object MathLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (MathContext) {

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
