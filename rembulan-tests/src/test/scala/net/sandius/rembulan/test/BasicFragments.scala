package net.sandius.rembulan.test

object BasicFragments {

  object IfThenElse extends Fragment {
    code =
        """if x >= 0 and x <= 10 then print(x) end
        """
  }

  object Upvalues1 extends Fragment {
    code =
        """
          |local x = {}
          |for i = 0, 10 do
          |  if i % 2 == 0 then x[i // 2] = function() return i, x end end
          |end
        """
  }

  object Upvalues2 extends Fragment {
    code =
        """
          |local x
          |x = 1
          |
          |local function sqr()
          |  return x * x
          |end
          |
          |x = 3
          |return sqr()
        """
  }

  object Upvalues3 extends Fragment {
    code =
        """local x, y
          |if g then
          |  y = function() return x end
          |else
          |  x = function() return y end
          |end
          |return x or y
        """
  }


  object BlockLocals extends Fragment {
    code =
        """do
          |  local a = 0
          |  local b = 1
          |end
          |
          |do
          |  local a = 2
          |end
        """
  }

  object Tailcalls extends Fragment {
    code =
        """function f(x)
          |  print(x)
          |  if x > 0 then
          |    return f(x - 1)
          |  else
          |    if x < 0 then
          |      return f(x + 1)
          |    else
          |      return 0
          |    end
          |  end
          |end
          |
          |return f(3),f(-2)
        """
  }

  object FuncWith2Params extends Fragment {
    code =
        """local f = function (x, y)
          |    return x + y
          |end
          |return -1 + f(1, 3) + 39
        """
  }

  object FuncWith3Params extends Fragment {
    code =
        """local f = function (x, y, z)
          |    return x + y + z
          |end
          |return -1 + f(1, 1, 2) + 39
        """
  }

  object DeterminateVarargs extends Fragment {
    code =
        """local a, b = ...
          |if a > 0 then
          |  return b, a
          |else
          |  return a, b
          |end
        """
  }

  object IndeterminateVarargs extends Fragment {
    code =
        """local a = ...
          |if a then
          |  return ...
          |else
          |  return false, ...
          |end
        """
  }

  object NilTestInlining extends Fragment {
    code =
        """local a
          |if a then
          |  return true
          |else
          |  return false
          |end
        """
  }

  object VarargFunctionCalls extends Fragment {
    code =
        """local f = function(...) return ... end
          |return true, f(...)
        """
  }

  object VarargFunctionCalls2 extends Fragment {
    code =
        """local x = ...
          |local f = function(...) return ... end
          |if x then
          |  return f(...)
          |else
          |  return true, f(...)
          |end
        """
  }

  object VarargDecomposition extends Fragment {
    code =
        """local a, b = ...
          |local c, d, e = a(b, ...)
          |return d(e, ...)
        """
  }

  object FunctionCalls extends Fragment {
    code =
        """local function f(x, y)
          |  return x + y
          |end
          |
          |return f(1, 2)
        """
  }

  object FunctionCalls2 extends Fragment {
    code =
        """local function abs(x)
          |  local function f(x, acc)
          |    if x > 0 then
          |      return f(x - 1, acc + 1)
          |    elseif x < 0 then
          |      return f(x + 1, acc + 1)
          |    else
          |      return acc
          |    end
          |  end
          |  local v = f(x, 0)
          |  return v
          |end
          |
          |return abs(20)
        """
  }

  object FunctionCalls3 extends Fragment {
    code =
        """local function abs(x)
          |  local function f(g, x, acc)
          |    if x > 0 then
          |      return g(g, x - 1, acc + 1)
          |    elseif x < 0 then
          |      return g(g, x + 1, acc + 1)
          |    else
          |      return acc
          |    end
          |  end
          |  local v = f(f, x, 0)
          |  return v
          |end
          |
          |return abs(20)
        """
  }

  object LocalUpvalue extends Fragment {
    code =
        """local function f()
          |  local x = 1
          |  local function g()
          |    return x * x
          |  end
          |  return g()
          |end
          |
          |return f()  -- equivalent to return 1
        """
  }

  object ReturningAFunction extends Fragment {
    code =
        """local function f()
          |  return function(x) return not not x, x end
          |end
          |
          |return f()()
        """
  }

  object IncompatibleFunctions extends Fragment {
    code =
        """local f
          |if x then
          |  f = function(x, y)
          |    local z = x or y
          |    return not not z, z
          |  end
          |else
          |  f = function()
          |    return x
          |  end
          |end
          |
          |return f(42)
        """
  }

  // test should fail
  object GotoLocalSlot_withX extends Fragment {
    code =
        """do
          | local k = 0
          | local x
          | ::foo::
          | local y
          | assert(not y)
          | y = true
          | k = k + 1
          | if k < 2 then goto foo end
          |end
        """
  }

  // test should fail, reported succeeding in Lua 5.2, 5.3
  object GotoLocalSlot_withoutX extends Fragment {
    code =
        """do
          | local k = 0
          | ::foo::
          | local y
          | assert(not y)
          | y = true
          | k = k + 1
          | if k < 2 then goto foo end
          |end
        """
  }

  object JustAdd extends Fragment {
    code =
        """return x + 1
        """
  }
}
