package net.sandius.rembulan.test

object BasicLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (BasicContext) {

    about ("type") {

      program ("return type(nil)") succeedsWith "nil"
      program ("return type(true)") succeedsWith "boolean"
      program ("return type(false)") succeedsWith "boolean"
      program ("return type(0)") succeedsWith "number"
      program ("return type(0.1)") succeedsWith "number"
      program ("return type(\"\")") succeedsWith "string"
      program ("return type(\"hello\")") succeedsWith "string"
      program ("return type(\"2\")") succeedsWith "string"
      program ("return type(\"0.2\")") succeedsWith "string"
      program ("return type(function() end)") succeedsWith "function"
      program ("return type(type)") succeedsWith "function"
      program ("return type({})") succeedsWith "table"

      program ("return type()") failsWith "bad argument #1 to 'type' (value expected)"

    }

    about ("tostring") {

      program ("return tostring(nil)") succeedsWith "nil"
      program ("return tostring(true)") succeedsWith "true"
      program ("return tostring(false)") succeedsWith "false"
      program ("return tostring(0)") succeedsWith "0"
      program ("return tostring(-0)") succeedsWith "0"
      program ("return tostring(0.0)") succeedsWith "0.0"
      program ("return tostring(-0.0)") succeedsWith "-0.0"
      program ("return tostring(\"\")") succeedsWith ""
      program ("return tostring(\"1\")") succeedsWith "1"
      program ("return tostring(\"1.00\")") succeedsWith "1.00"

      program ("return tostring(1 / 0)") succeedsWith "inf"
      program ("return tostring(-1 / 0)") succeedsWith "-inf"
      program ("return tostring(0 / 0)") succeedsWith "nan"

      program ("return tostring(function() end)") succeedsWith (stringStartingWith("function: "))
      program ("return tostring(tostring)") succeedsWith (stringStartingWith("function: "))
      program ("return tostring({})") succeedsWith (stringStartingWith("table: "))

      program ("return tostring()") failsWith "bad argument #1 to 'tostring' (value expected)"

    }

    about ("_VERSION") {
      program ("return _VERSION") succeedsWith "Lua 5.3"
    }

    about ("tonumber") {

      program ("return tonumber(nil)") succeedsWith null
      program ("return tonumber(1)") succeedsWith 1
      program ("return tonumber(0.3)") succeedsWith 0.3
      program ("return tonumber(0)") succeedsWith 0
      program ("return tonumber(0.0)") succeedsWith 0.0
      program ("return tonumber(\"x\")") succeedsWith null
      program ("return tonumber(\"2\")") succeedsWith 2
      program ("return tonumber(\"0.4\")") succeedsWith 0.4
      program ("return tonumber(\"3.0\")") succeedsWith 3.0
      program ("return tonumber({})") succeedsWith null

      program ("tonumber(1, \"x\")") failsWith "bad argument #2 to 'tonumber' (number expected, got string)"

      program ("tonumber(\"1\", 1)") failsWith "bad argument #2 to 'tonumber' (base out of range)"
      program ("tonumber(\"1\", 37)") failsWith "bad argument #2 to 'tonumber' (base out of range)"

      program ("tonumber(1, 1)") failsWith "bad argument #1 to 'tonumber' (string expected, got number)"
      program ("tonumber(nil, 10)") failsWith "bad argument #1 to 'tonumber' (string expected, got nil)"
      program ("tonumber(nil, 1)") failsWith "bad argument #1 to 'tonumber' (string expected, got nil)"

      program ("return tonumber(\"-AbCd\", 14)") succeedsWith -29777
      program ("return tonumber(\"+Hello\", 36)") succeedsWith 29234652
      program ("return tonumber(\" spaces  \", 36)") succeedsWith 1735525972
      program ("return tonumber(\"spaces\", 36)") succeedsWith 1735525972
      program ("return tonumber(\"A0\", 10)") succeedsWith null
      program ("return tonumber(\"99\", 9)") succeedsWith null
      program ("return tonumber(\"zzz\", 36)") succeedsWith 46655

      program ("return tonumber(1 / 0, 36)") failsWith "bad argument #1 to 'tonumber' (string expected, got number)"
      program ("return tonumber(0 / 0, 36)") failsWith "bad argument #1 to 'tonumber' (string expected, got number)"
      program ("return tonumber(0.2, 10)") failsWith "bad argument #1 to 'tonumber' (string expected, got number)"

    }

    about ("getmetatable") {

      program ("return getmetatable(nil)") succeedsWith null
      program ("return getmetatable(true)") succeedsWith null
      program ("return getmetatable(0)") succeedsWith null
      program ("return getmetatable(\"hurray\")") succeedsWith null  // defined by the string library

      program ("getmetatable()") failsWith "bad argument #1 to 'getmetatable' (value expected)"

    }

    about ("setmetatable") {

      program ("setmetatable(0, nil)") failsWith "bad argument #1 to 'setmetatable' (table expected, got number)"
      program ("setmetatable({}, 0)") failsWith "bad argument #2 to 'setmetatable' (nil or table expected)"
      program ("setmetatable({})") failsWith "bad argument #2 to 'setmetatable' (nil or table expected)"

      val SetMetatableReturnsItsFirstArgument = fragment("setmetatable returns its first argument") {
          """local x = {}
            |local y = setmetatable(x, {})
            |return x == y, x == {}
          """
      }
      SetMetatableReturnsItsFirstArgument in thisContext succeedsWith (true, false)

      val SetMetatableAndGetMetatable = fragment("setmetatable and getmetatable") {
        """local t = {}
          |local mt0 = getmetatable(t)
          |local mt1 = {}
          |setmetatable(t, mt1)
          |local mt2 = getmetatable(t)
          |setmetatable(t, nil)
          |local mt3 = getmetatable(t)
          |return mt0 == nil, mt2 == mt1, mt2 == {}, mt3 == nil
        """
      }
      SetMetatableAndGetMetatable in thisContext succeedsWith (true, true, false, true)

      program (
        """mt = {}
          |t = {}
          |setmetatable(t, mt)
          |mt.__metatable = 'hello'
          |return getmetatable(t)
        """) succeedsWith "hello"

    }

    about ("pcall") {

      program ("return pcall(nil)") succeedsWith (false, "attempt to call a nil value")
      program ("return pcall(function() end)") succeedsWith true
      program ("return pcall(pcall)") succeedsWith (false, "bad argument #1 to 'pcall' (value expected)")
      program ("return pcall(pcall, pcall, pcall)") succeedsWith (true, true, false, "bad argument #1 to 'pcall' (value expected)")

      program ("pcall()") failsWith "bad argument #1 to 'pcall' (value expected)"

      val PCallHonoursTheCallMetamethod = fragment ("pcall honours the __call metamethod") {
        """function callable()
          |  local mt = {}
          |  mt.__call = function() return 42 end
          |  local t = {}
          |  setmetatable(t, mt)
          |  return t
          |end
          |
          |x = callable()
          |return pcall(x)
        """
      }
      PCallHonoursTheCallMetamethod in thisContext succeedsWith (true, 42)

      val PCallCatchesErrorInACallMetamethod = fragment ("pcall catches error in a __call metamethod") {
        """function callable()
          |  local mt = {}
          |  mt.__call = function() error('kaboom') end
          |  local t = {}
          |  setmetatable(t, mt)
          |  return t
          |end
          |
          |x = callable()
          |return pcall(x)
        """
      }
      // FIXME: the error object should actually be "stdin:3: kaboom"
      PCallCatchesErrorInACallMetamethod in thisContext succeedsWith (false, "kaboom")

    }

    about ("xpcall") {

      program ("xpcall()") failsWith "bad argument #2 to 'xpcall' (function expected, got no value)"
      program ("return xpcall(nil)") failsWith "bad argument #2 to 'xpcall' (function expected, got no value)"
      program ("return xpcall(function() end)") failsWith "bad argument #2 to 'xpcall' (function expected, got no value)"
      program ("return xpcall(nil, nil)") failsWith "bad argument #2 to 'xpcall' (function expected, got nil)"

      program ("return xpcall(nil, function(...) return ... end)") succeedsWith (false, "attempt to call a nil value")

      program ("return xpcall(xpcall, pcall)") succeedsWith (false, false)
      program ("return xpcall(pcall, xpcall)") succeedsWith (false, "error in error handling")

      program (
        """count = 0
          |function handle(eo)
          |  count = count + 1
          |  error(eo)
          |end
          |xpcall(nil, handle)
          |return count
        """) succeedsWith 220

    }

    about ("error") {

      program ("return error()") failsWithLuaError (null)
      program ("return error(nil)") failsWithLuaError (null)

      program ("error(1)") failsWithLuaError (java.lang.Long.valueOf(1))
      program ("error(1.0)") failsWithLuaError (java.lang.Double.valueOf(1.0))
      program ("error(\"boom\")") failsWithLuaError "boom"

      program ("return pcall(error)") succeedsWith (false, null)

    }

    about ("assert") {
      program ("assert(nil)") failsWith "assertion failed!"
      program ("assert(false, 'boom')") failsWith "boom"

      program ("return assert(true)") succeedsWith true
      program ("return assert(1, false, 'x')") succeedsWith (1, false, "x")

      program ("assert()") failsWith "bad argument #1 to 'assert' (value expected)"

      program ("assert(pcall(error, 'boom'))") failsWith "boom"
    }

    about ("rawequal") {
      program ("return rawequal()") failsWith "bad argument #1 to 'rawequal' (value expected)"
      program ("return rawequal(nil)") failsWith "bad argument #2 to 'rawequal' (value expected)"

      program ("return rawequal(nil, nil)") succeedsWith true
      program ("return rawequal(0, 0)") succeedsWith true
      program ("return rawequal(0 / 0, 0 / 0)") succeedsWith false

      // TODO: add tests for values that do have the __eq metamethod
    }

    about ("rawget") {
      program ("rawget()") failsWith "bad argument #1 to 'rawget' (table expected, got no value)"
      program ("rawget(nil)") failsWith "bad argument #1 to 'rawget' (table expected, got nil)"
      program ("rawget('x')") failsWith "bad argument #1 to 'rawget' (table expected, got string)"

      program (
        """x = {}
          |x.hello = 'world'
          |return rawget(x, 'hello')
        """) succeedsWith "world"

      // TODO: add tests for values that do have the __index metamethod
    }

    about ("rawset") {
      program ("rawset()") failsWith "bad argument #1 to 'rawset' (table expected, got no value)"
      program ("rawset(nil)") failsWith "bad argument #1 to 'rawset' (table expected, got nil)"
      program ("rawset('x')") failsWith "bad argument #1 to 'rawset' (table expected, got string)"

      program ("rawset({}, nil)") failsWith "bad argument #3 to 'rawset' (value expected)"

      program (
        """x = {}
          |rawset(x, 'hello', 'world')
          |return x.hello
        """) succeedsWith "world"

      program ("rawset({}, nil, 1)") failsWith "table index is nil"
      program ("rawset({}, 0 / 0, 1)") failsWith "table index is NaN"

      program (
        """x = {}
          |y = rawset(x, 0, 'hi')
          |return x == y
        """) succeedsWith true

      // TODO: add tests for values that do have the __newindex metamethod
    }

    about ("rawlen") {

      program ("rawlen()") failsWith "bad argument #1 to 'rawlen' (table or string expected)"
      program ("rawlen(1)") failsWith "bad argument #1 to 'rawlen' (table or string expected)"

      program ("return rawlen('x')") succeedsWith 1
      program ("return rawlen({'x', 1, true})") succeedsWith 3

      // TODO: add tests for values that do have the __len metamethod
    }

    about ("select") {
      program ("select()") failsWith "bad argument #1 to 'select' (number expected, got no value)"

      program ("select('x')") failsWith "bad argument #1 to 'select' (number expected, got string)"
      program ("select(' #')") failsWith "bad argument #1 to 'select' (number expected, got string)"
      program ("select(' # ')") failsWith "bad argument #1 to 'select' (number expected, got string)"

      program ("return select('#')") succeedsWith 0
      program ("return select('#', nil)") succeedsWith 1
      program ("return select('#', 1, 2, 3, 4, 5)") succeedsWith 5

      program ("return select('+1', true, false)") succeedsWith (true, false)
      program ("return select('-1', true, false)") succeedsWith (false)

      program ("return select(7, true, false)") succeedsWith ()
      program ("select(0, true, false)") failsWith "bad argument #1 to 'select' (index out of range)"
      program ("select(-3, true, false)") failsWith "bad argument #1 to 'select' (index out of range)"

      program ("select(1.5, true, false)") failsWith "bad argument #1 to 'select' (number has no integer representation)"

      program ("return select(1, 1, 2, 3, 4, 5)") succeedsWith (1, 2, 3, 4, 5)
      program ("return select(-1, 1, 2, 3, 4, 5)") succeedsWith (5)
      program ("return select(2, 1, 2, 3, 4, 5)") succeedsWith (2, 3, 4, 5)
      program ("return select(3, 1, 2, 3, 4, 5)") succeedsWith (3, 4, 5)
      program ("return select(-2, 1, 2, 3, 4, 5)") succeedsWith (4, 5)
      program ("return select(-3, 1, 2, 3, 4, 5)") succeedsWith (3, 4, 5)
    }


  }

}
