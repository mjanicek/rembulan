package net.sandius.rembulan.test

object BasicFragments extends FragmentBundle {

  fragment ("JustX") {
    """return x
    """
  }

  fragment ("NotX") {
    """return not x
    """
  }

  fragment ("NotTrue") {
    """return not true
    """
  }

  fragment ("NotNotX") {
    """return not not x
    """
  }

  fragment ("JustAdd") {
    """return x + 1
    """
  }

  fragment ("AddNumbers") {
    """local a = 39
      |local b = 3.0
      |return a + b
    """
  }

  fragment ("IfThenElse") {
    """if x >= 0 and x <= 10 then print(x) end
    """
  }

  fragment ("Upvalues1") {
    """local x = {}
      |for i = 0, 10 do
      |  if i % 2 == 0 then x[i // 2] = function() return i, x end end
      |end
    """
  }

  fragment ("Upvalues2") {
    """local x
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

  fragment ("Upvalues3") {
    """local x, y
      |if g then
      |  y = function() return x end
      |else
      |  x = function() return y end
      |end
      |return x or y
    """
  }

  fragment ("SetUpvalue") {
    """local x = 1
      |
      |local function f()
      |  x = 123
      |end
      |
      |f()
      |return x
    """
  }

  fragment ("SetTabUp") {
    """x = 1
      |return x
    """
  }

  fragment ("Tables") {
    """local t = {}
      |t.self = t
      |return t.self
    """
  }

  fragment ("Self") {
    """local function GET(tab, k) return tab[k] end
      |local function SET(tab, k, v) tab[k] = v end
      |
      |local t = {}
      |t.get = GET
      |t.set = SET
      |
      |local before = t:get(1)
      |t:set(1, "hello")
      |local after = t:get(1)
      |
      |return before, after
    """
  }

  fragment ("BlockLocals") {
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

  fragment ("Tailcalls") {
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

  fragment ("FuncWith2Params") {
    """local f = function (x, y)
      |    return x + y
      |end
      |return -1 + f(1, 3) + 39
    """
  }

  fragment ("FuncWith3Params") {
    """local f = function (x, y, z)
      |    return x + y + z
      |end
      |return -1 + f(1, 1, 2) + 39
    """
  }

  fragment ("DeterminateVarargs") {
    """local a, b = ...
      |if a > 0 then
      |  return b, a
      |else
      |  return a, b
      |end
    """
  }

  fragment ("ReturnVarargs") {
    """return ...
    """
  }


  fragment ("IndeterminateVarargs") {
    """local a = ...
      |if a then
      |  return ...
      |else
      |  return false, ...
      |end
    """
  }

  fragment ("NilTestInlining") {
    """local a
      |if a then
      |  return true
      |else
      |  return false
      |end
    """
  }

  fragment ("VarargFunctionCalls") {
    """local f = function(...) return ... end
      |return true, f(...)
    """
  }

  fragment ("VarargFunctionCalls2") {
    """local x = ...
      |local f = function(...) return ... end
      |if x then
      |  return f(...)
      |else
      |  return true, f(...)
      |end
    """
  }

  fragment ("VarargDecomposition") {
    """local a, b = ...
      |local c, d, e = a(b, ...)
      |return d(e, ...)
    """
  }

  fragment ("FunctionCalls") {
    """local function f(x, y)
      |  return x + y
      |end
      |
      |return f(1, 2)
    """
  }

  fragment ("FunctionCalls2") {
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

  fragment ("FunctionCalls3") {
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

  fragment ("LocalUpvalue") {
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

  fragment ("ReturningAFunction") {
    """local function f()
      |  return function(x) return not not x, x end
      |end
      |
      |return f()()
    """
  }

  fragment ("IncompatibleFunctions") {
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
  fragment ("GotoLocalSlot_withX") {
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
  fragment ("GotoLocalSlot_withoutX") {
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

}
