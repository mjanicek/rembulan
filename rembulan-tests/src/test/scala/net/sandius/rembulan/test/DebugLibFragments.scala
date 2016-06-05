package net.sandius.rembulan.test

import net.sandius.rembulan.core.Table

object DebugLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (DebugContext) {

    about ("debug.getmetatable") {

      program ("""return debug.getmetatable()""") failsWith "bad argument #1 to 'getmetatable' (value expected)"
      program ("""return debug.getmetatable(1)""") succeedsWith (null)

    }

    about ("debug.setmetatable") {

      program ("""return debug.setmetatable()""") failsWith "bad argument #2 to 'setmetatable' (nil or table expected)"
      program ("""return debug.setmetatable(1)""") failsWith "bad argument #2 to 'setmetatable' (nil or table expected)"
      program ("""return debug.setmetatable(1, 2)""") failsWith "bad argument #2 to 'setmetatable' (nil or table expected)"

      program ("""local x; return debug.setmetatable(1, x)""") succeedsWith (1)
      program ("""return debug.setmetatable(1, {})""") succeedsWith (1)

    }

  }

  in (FullContext) {

    about ("debug.{get|set}metatable") {

      val setup = List(
        "boolean" -> ("true", "false"),
        "string" -> ("\"hello\"", "\"world\""),
        "function" -> ("function() return 1 end", "function(x) return x * x end"),
        "thread" -> ("coroutine.create(pcall)", "coroutine.create(error)")
      )

      for ((tpe, (setVarValue, getVarValue)) <- setup) {
        val setVar = "x"
        val getVar = "y"

        val frag = fragment (s"metatables updated for ${tpe}s") {
          s"""local mt = {}
             |local $setVar = $setVarValue
             |local $getVar = $getVarValue
             |debug.setmetatable($setVar, mt)
             |local got = debug.getmetatable($getVar)
             |return $setVar == $getVar, got, got == mt
           """
        }
        frag in thisContext succeedsWith (false, classOf[Table], true)
      }

      val NilMetatableUpdated = fragment ("metatable updated for nil") {
        """local mt = {}
          |local x
          |debug.setmetatable(x, mt)
          |local got = debug.getmetatable(x, mt)
          |return got, got == mt
        """
      }
      NilMetatableUpdated in thisContext succeedsWith (classOf[Table], true)

      val NumerMetatableUpdated = fragment ("metatables updated for numbers") {
        """local mt = {}
          |debug.setmetatable(0, mt)
          |local ints, floats = debug.getmetatable(1), debug.getmetatable(1/0)
          |return ints, floats, ints == floats, ints == mt
        """
      }
      NumerMetatableUpdated in thisContext succeedsWith (classOf[Table], classOf[Table], true, true)

      val TableMetatableUpdated = fragment (s"metatables updated for tables") {
        s"""local mt = {}
           |local u = {}
           |debug.setmetatable(u, mt)
           |local got = debug.getmetatable(u)
           |return got, got == mt
         """
      }
      TableMetatableUpdated in thisContext succeedsWith (classOf[Table], true)

    }

  }

}
