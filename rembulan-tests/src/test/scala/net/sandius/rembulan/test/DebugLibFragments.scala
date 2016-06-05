package net.sandius.rembulan.test

import net.sandius.rembulan.core.Table
import net.sandius.rembulan.{core => lua}

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

    about ("debug.getupvalue") {

      program ("""return debug.getupvalue()""") failsWith "bad argument #2 to 'getupvalue' (number expected, got no value)"
      program ("""return debug.getupvalue(2)""") failsWith "bad argument #2 to 'getupvalue' (number expected, got no value)"
      program ("""return debug.getupvalue(2, 1.2)""") failsWith "bad argument #2 to 'getupvalue' (number has no integer representation)"
      program ("""return debug.getupvalue(1, 1)""") failsWith "bad argument #1 to 'getupvalue' (function expected, got number)"

      program ("""return debug.getupvalue(function() return x end, 0)""") succeedsWith ()
      program ("""return debug.getupvalue(function() return x end, 1)""") succeedsWith ("_ENV", classOf[Table])
      program ("""return debug.getupvalue(function() return x end, 2)""") succeedsWith ()

      val TwoENVFunctions = fragment ("retrieves the same value for two functions that reference _ENV") {
        """local f = function() return x end
          |local g = function() return y end
          |local nf, vf = debug.getupvalue(f, 1)
          |local ng, vg = debug.getupvalue(g, 1)
          |return nf, vf, ng, vg, vf == vg
        """
      }
      TwoENVFunctions in thisContext succeedsWith ("_ENV", classOf[Table], "_ENV", classOf[Table], true)

      val TwoNonENVFunctions = fragment ("retrieves the correct upvalue values for non _ENV referencing functions") {
        """local f = function() return 42 end
          |local g = function() return f() end
          |local nf, vf = debug.getupvalue(f, 1)
          |local ng, vg = debug.getupvalue(g, 1)
          |return nf, vf, ng, vg, vg == f
        """
      }
      TwoNonENVFunctions in thisContext succeedsWith (null, null, "f", classOf[lua.Function], true)

    }

    about ("debug.setupvalue") {

      program ("""return debug.setupvalue()""") failsWith "bad argument #3 to 'setupvalue' (value expected)"
      program ("""return debug.setupvalue(2)""") failsWith "bad argument #3 to 'setupvalue' (value expected)"
      program ("""return debug.setupvalue(2, 1.2)""") failsWith "bad argument #3 to 'setupvalue' (value expected)"
      program ("""return debug.setupvalue(x, 1.2, x)""") failsWith "bad argument #2 to 'setupvalue' (number has no integer representation)"
      program ("""return debug.setupvalue(1, 1, 1)""") failsWith "bad argument #1 to 'setupvalue' (function expected, got number)"

      val UpdatesLocalVariableValue = fragment ("updates local variable value") {
        """local x = 42
          |local f = function() return x end
          |local name = debug.setupvalue(f, 1, "works!")
          |return name, x, f()
        """
      }
      UpdatesLocalVariableValue in thisContext succeedsWith ("x", "works!", "works!")

      val UpdatesUpvalueValue = fragment ("updates the upvalue value") {
        """local x = 42
          |local f = function() return x end
          |local name = debug.setupvalue(f, 1, "works!")
          |x = 1234.5
          |return name, x, f()
        """
      }
      UpdatesUpvalueValue in thisContext succeedsWith ("x", 1234.5, 1234.5)

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
