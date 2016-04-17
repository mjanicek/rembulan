package net.sandius.rembulan.test

import net.sandius.rembulan.core._
import net.sandius.rembulan.{core => lua}

object BasicFragments extends FragmentBundle with FragmentExpectations {

  val JustX = fragment ("JustX") {
    """return x
    """
  }
  JustX in EmptyContext succeedsWith (null)

  val NotX = fragment ("NotX") {
    """return not x
    """
  }
  NotX in EmptyContext succeedsWith (true)

  val NotTrue = fragment ("NotTrue") {
    """return not true
    """
  }
  NotTrue in EmptyContext succeedsWith (false)

  val NotNotX = fragment ("NotNotX") {
    """return not not x
    """
  }
  NotNotX in EmptyContext succeedsWith (false)

  val JustAdd = fragment ("JustAdd") {
    """return x + 1
    """
  }
  JustAdd in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to perform arithmetic on a nil value")

  val AddNumbers = fragment ("AddNumbers") {
    """local a = 39
      |local b = 3.0
      |return a + b
    """
  }
  AddNumbers in EmptyContext succeedsWith (42.0)

  val IfThenElse = fragment ("IfThenElse") {
    """if x >= 0 and x <= 10 then print(x) end
    """
  }
  IfThenElse in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to compare number with nil")

  val IntegerCmp = fragment ("IntegerCmp") {
    """local min = 0x8000000000000000
      |local max = 0x7fffffffffffffff
      |return min < max, max + 1 == min, min < 0, max > 0
    """
  }
  IntegerCmp in EmptyContext succeedsWith (true, true, true, true)

  val NumericCmp = fragment ("NumericCmp") {
    """return 1 < 1.0, 1 > 1.0, 1 <= 1.0, 1 >= 1.0, 1 == 1.0
    """
  }
  NumericCmp in EmptyContext succeedsWith (false, false, true, true, true)

  val NaNCmp = fragment ("NaNCmp") {
    """local nan = 0/0
      |return 0 < nan, 0 > nan, nan == nan, nan < nan, nan > nan, nan >= nan, nan <= nan
    """
  }
  NaNCmp in EmptyContext succeedsWith (false, false, false, false, false, false, false)

  val StringCmp = fragment ("StringCmp") {
    """return "hello" < "there", "1" < "1.0", "1" > "1.0", "1" == "1.0"
    """
  }
  StringCmp in EmptyContext succeedsWith (true, true, false, false)

  val MixedEq = fragment ("MixedEq") {
    """return 1 == "1", "1" == 1.0, 1 == 1.0
    """
  }
  MixedEq in EmptyContext succeedsWith (false, false, true)

  val MixedCmp = fragment ("MixedCmp") {
    """return 1 < "1"
    """
  }
  MixedCmp in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to compare number with string")

  val MixedCmpReverse = fragment ("MixedCmpReverse") {
    """return 1 > "1"
    """
  }
  MixedCmpReverse in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to compare string with number")

  val SimpleForLoop = fragment("SimpleForLoop") {
    """local sum = 0
      |for i = 1, 10 do
      |  sum = sum + i
      |end
      |return sum
    """
  }
  SimpleForLoop in EmptyContext succeedsWith (55)

  val FloatForLoop = fragment("FloatForLoop") {
    """local sum = 0
      |for i = 1.0, 9.9, 0.4 do
      |  sum = sum + i
      |end
      |return sum
    """
  }
  expect {
    var sum = 0.0
    for (d <- 1.0 to 9.9 by 0.4) { sum += d }
    FloatForLoop in EmptyContext succeedsWith (sum)
  }

  val MixedNumericForLoop = fragment ("MixedNumericForLoop") {
    """local sum = 0
      |for i = 1, 10.0 do
      |  sum = sum + i
      |end
      |return sum
    """
  }
  MixedNumericForLoop in EmptyContext succeedsWith (55)

  val RuntimeDeterminedForLoop = fragment ("RuntimeDeterminedForLoop") {
    """local sum = 0
      |for i = 1, "10" do
      |  sum = sum + i
      |end
      |return sum
    """
  }
  RuntimeDeterminedForLoop in EmptyContext succeedsWith (55)

  val IllegalForLoop1 = fragment ("IllegalForLoop1") {
    """for i = "a", "b", "c" do end
    """
  }
  IllegalForLoop1 in EmptyContext failsWith(classOf[IllegalArgumentException], "'for' limit must be a number")

  val IllegalForLoop2 = fragment ("IllegalForLoop2") {
    """for i = "a", 0, "c" do end
    """
  }
  IllegalForLoop2 in EmptyContext failsWith(classOf[IllegalArgumentException], "'for' step must be a number")

  val IllegalForLoop3 = fragment ("IllegalForLoop3") {
    """for i = "a", 0, 0 do end
    """
  }
  IllegalForLoop3 in EmptyContext failsWith(classOf[IllegalArgumentException], "'for' initial value must be a number")

  val IllegalForLoop4 = fragment ("IllegalForLoop4") {
    """for i = 1, "x" do end
    """
  }
  IllegalForLoop4 in EmptyContext failsWith(classOf[IllegalArgumentException], "'for' limit must be a number")

  val NaNForLoop = fragment ("NaNForLoop") {
    """local n = 0
      |for i = 0, (0/0) do
      |  n = n + 1.0
      |end
      |return n
    """
  }
  NaNForLoop in EmptyContext succeedsWith (0)

  val ZeroStepForLoop = fragment ("ZeroStepForLoop") {
    """for i = 1, 10, 0 do assert(false) end
    """
  }
  ZeroStepForLoop in EmptyContext succeedsWith ()
  ZeroStepForLoop in BaseLibContext succeedsWith ()

  val ZeroStepFloatForLoop = fragment ("ZeroStepFloatForLoop") {
    """for i = 1, 10, 0.0 do assert(false) end
    """
  }
  ZeroStepFloatForLoop in EmptyContext succeedsWith ()
  ZeroStepFloatForLoop in BaseLibContext succeedsWith ()

  val NegativeStepForLoop = fragment ("NegativeStepForLoop") {
    """for i = 1, 10, -1 do assert(false) end
    """
  }
  NegativeStepForLoop in EmptyContext succeedsWith ()
  NegativeStepForLoop in BaseLibContext succeedsWith ()

  val NegativeStepFloatForLoop = fragment ("NegativeStepFloatForLoop") {
    """for i = 0, 1, -1.0 do assert(false) end
    """
  }
  NegativeStepFloatForLoop in EmptyContext succeedsWith ()
  NegativeStepFloatForLoop in BaseLibContext succeedsWith ()

  val SimplifiableFloatForLoop = fragment ("SimplifiableFloatForLoop") {
    """local step = -1.0
      |for i = 0, 5, step do  -- loop type (negative, non-NaN) can be determined at compile time
      |  assert(false)
      |end
    """
  }
  SimplifiableFloatForLoop in EmptyContext succeedsWith ()
  SimplifiableFloatForLoop in BaseLibContext succeedsWith ()

  val DynamicIntegerForLoop = fragment ("DynamicIntegerForLoop") {
    """local function forloop(init, limit, step, f)
      |  for i = init, limit, step do
      |    f(i)
      |  end
      |end
      |
      |local sum = 0
      |forloop(1, 10, 1, function(i) sum = sum + i end)
      |return sum
    """
  }
  DynamicIntegerForLoop in EmptyContext succeedsWith (55)

  val ForLoopMtAttempt = fragment ("ForLoopMtAttempt") {
    """local function nt(v)
      |  local t = {}
      |  local f = function(a, b) return v end
      |  setmetatable(t, { __add = f, __sub = f })
      |  return t
      |end
      |
      |for i = nt(0), 10 do assert(false) end
    """
  }
  ForLoopMtAttempt in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to call a nil value")
  ForLoopMtAttempt in BaseLibContext failsWith (classOf[IllegalOperationAttemptException], "'for' initial value must be a number")

  val BitwiseOps = fragment ("BitwiseOps") {
    """local x = 3
      |local y = 10
      |
      |return x & y, x | y, x ~ y, ~x, ~y, x << y, x >> y
    """
  }
  BitwiseOps in EmptyContext succeedsWith (2, 11, 9, -4, -11, 3072, 0)

  val BitwiseCoercedOps = fragment ("BitwiseCoercedOps") {
    """local x = 3.0
      |local y = 10.0
      |return x & y, x | y
    """
  }
  BitwiseCoercedOps in EmptyContext succeedsWith (2, 11)

  val BitwiseStringCoercedOps = fragment ("BitwiseStringCoercedOps") {
    """local x = "3"
      |local y = "10.0"
      |return x & y, x | y, x ~ y, ~x, ~y, x << y, x >> y
    """
  }
  BitwiseStringCoercedOps in EmptyContext succeedsWith (2, 11, 9, -4, -11, 3072, 0)

  val BitwiseAttemptError = fragment ("BitwiseAttemptError") {
    """return x & y
    """
  }
  BitwiseAttemptError in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to perform bitwise operation on a nil value")

  val BitwiseRepresentationError = fragment ("BitwiseRepresentationError") {
    """local function int(x)
      |  return x & -1
      |end
      |int(3.1)
    """
  }
  BitwiseRepresentationError in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "number has no integer representation")

  val BitwiseError = fragment ("BitwiseError") {
    """local x = print or 1.2
      |return 10 & x
    """
  }
  BitwiseError in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "number has no integer representation")
  BitwiseError in BaseLibContext failsWith (classOf[IllegalOperationAttemptException], "attempt to perform bitwise operation on a function value")

  val UnmOnNumbers = fragment ("UnmOnNumbers") {
    """local i = 42
      |local f = 42.6
      |local function fun()
      |  if assert then return 314 else return 0.314 end
      |end
      |local n = fun()
      |
      |return i, -i, f, -f, n, -n
    """
  }
  UnmOnNumbers in EmptyContext succeedsWith (42, -42, 42.6, -42.6, 0.314, -0.314)
  UnmOnNumbers in BaseLibContext succeedsWith (42, -42, 42.6, -42.6, 20, -20)

  val UnmOnNumericString = fragment ("UnmOnNumericString") {
    """local i = "42"
      |local f = "42.6"
      |
      |return i, -i, f, -f
    """
  }
  UnmOnNumericString in EmptyContext succeedsWith ("42", -42.0, "42.6", -42.6)

  val UnmOnNonNumericString = fragment ("UnmOnNonNumericString") {
    """local s = "hello"
      |return -s
    """
  }
  UnmOnNonNumericString in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to perform arithmetic on a string value")

  val UnmOnNil = fragment ("UnmOnNil") {
    """return -x
    """
  }
  UnmOnNil in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to perform arithmetic on a nil value")

  val StringLength = fragment ("StringLength") {
    """local s = "hello"
      |return #s
    """
  }
  StringLength in EmptyContext succeedsWith (5)

  val SeqTableLength = fragment ("SeqTableLength") {
    """local t = {}
      |t[1] = "hi"
      |t[2] = "there"
      |return #t
    """
  }
  SeqTableLength in EmptyContext succeedsWith (2)

  val NilTableLength = fragment ("NilTableLength") {
    """return #t
    """
  }
  NilTableLength in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to get length of a nil value")

  val ConcatStrings = fragment ("ConcatStrings") {
    """return "hello".." ".."world"
    """
  }
  ConcatStrings in EmptyContext succeedsWith ("hello world")

  val ConcatStringsAndNumbers = fragment ("ConcatStringsAndNumbers") {
    """return (4 .. 1 + 1.0) .. " = " .. 42
    """
  }
  ConcatStringsAndNumbers in EmptyContext succeedsWith ("42.0 = 42")

  val ConcatDynamic = fragment ("ConcatDynamic") {
    """local function c(a, b, c)
      |  return a..b..c
      |end
      |
      |local s = c(1, "2", c(0, 0, 0))
      |return s
    """
  }
  ConcatDynamic in EmptyContext succeedsWith ("12000")

  val ConcatNil = fragment ("ConcatNil") {
    """return "x = "..x
    """
  }
  ConcatNil in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to concatenate a nil value")

  val Upvalues1 = fragment ("Upvalues1") {
    """local x = {}
      |for i = 0, 10 do
      |  if i % 2 == 0 then x[i // 2] = function() return i, x end end
      |end
    """
  }
  Upvalues1 in EmptyContext succeedsWith ()

  val Upvalues2 = fragment ("Upvalues2") {
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
  Upvalues2 in EmptyContext succeedsWith (9)

  val Upvalues3 = fragment ("Upvalues3") {
    """local x, y
      |if g then
      |  y = function() return x end
      |else
      |  x = function() return y end
      |end
      |return x or y
    """
  }
  Upvalues3 in EmptyContext succeedsWith (classOf[lua.Function])

  val SetUpvalue = fragment ("SetUpvalue") {
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
  SetUpvalue in EmptyContext succeedsWith (123)

  val SetTabUp = fragment ("SetTabUp") {
    """x = 1
      |return x
    """
  }
  SetTabUp in EmptyContext succeedsWith (1)

  val Tables = fragment ("Tables") {
    """local t = {}
      |t.self = t
      |return t.self
    """
  }
  Tables in EmptyContext succeedsWith (classOf[lua.Table])

  val Self = fragment ("Self") {
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
  Self in EmptyContext succeedsWith (null, "hello")

  val BlockLocals = fragment ("BlockLocals") {
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
  BlockLocals in EmptyContext succeedsWith ()

  val Tailcalls = fragment ("Tailcalls") {
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
  Tailcalls in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to call a nil value")

  val FuncWith2Params = fragment ("FuncWith2Params") {
    """local f = function (x, y)
      |    return x + y
      |end
      |return -1 + f(1, 3) + 39
    """
  }
  FuncWith2Params in EmptyContext succeedsWith (42)

  val FuncWith3Params = fragment ("FuncWith3Params") {
    """local f = function (x, y, z)
      |    return x + y + z
      |end
      |return -1 + f(1, 1, 2) + 39
    """
  }
  FuncWith3Params in EmptyContext succeedsWith (42)

  val DeterminateVarargs = fragment ("DeterminateVarargs") {
    """local a, b = ...
      |if a > 0 then
      |  return b, a
      |else
      |  return a, b
      |end
    """
  }
  DeterminateVarargs in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to compare number with nil")

  val ReturnVarargs = fragment ("ReturnVarargs") {
    """return ...
    """
  }
  ReturnVarargs in EmptyContext succeedsWith ()

  val IndeterminateVarargs = fragment ("IndeterminateVarargs") {
    """local a = ...
      |if a then
      |  return ...
      |else
      |  return false, ...
      |end
    """
  }
  IndeterminateVarargs in EmptyContext succeedsWith (false)

  val NilTestInlining = fragment ("NilTestInlining") {
    """local a
      |if a then
      |  return true
      |else
      |  return false
      |end
    """
  }
  NilTestInlining in EmptyContext succeedsWith (false)

  val VarargFunctionCalls = fragment ("VarargFunctionCalls") {
    """local f = function(...) return ... end
      |return true, f(...)
    """
  }
  VarargFunctionCalls in EmptyContext succeedsWith (true)

  val VarargFunctionCalls2 = fragment ("VarargFunctionCalls2") {
    """local x = ...
      |local f = function(...) return ... end
      |if x then
      |  return f(...)
      |else
      |  return true, f(...)
      |end
    """
  }
  VarargFunctionCalls2 in EmptyContext succeedsWith (true)

  val VarargDecomposition = fragment ("VarargDecomposition") {
    """local a, b = ...
      |local c, d, e = a(b, ...)
      |return d(e, ...)
    """
  }
  VarargDecomposition in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to call a nil value")

  val FunctionCalls = fragment ("FunctionCalls") {
    """local function f(x, y)
      |  return x + y
      |end
      |
      |return f(1, 2)
    """
  }
  FunctionCalls in EmptyContext succeedsWith (3)

  val FunctionCalls2 = fragment ("FunctionCalls2") {
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
  FunctionCalls2 in EmptyContext succeedsWith (20)

  val FunctionCalls3 = fragment ("FunctionCalls3") {
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
  FunctionCalls3 in EmptyContext succeedsWith (20)

  val LocalUpvalue = fragment ("LocalUpvalue") {
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
  LocalUpvalue in EmptyContext succeedsWith (1)

  val ReturningAFunction = fragment ("ReturningAFunction") {
    """local function f()
      |  return function(x) return not not x, x end
      |end
      |
      |return f()()
    """
  }
  ReturningAFunction in EmptyContext succeedsWith (false, null)

  val IncompatibleFunctions = fragment ("IncompatibleFunctions") {
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
  IncompatibleFunctions in EmptyContext succeedsWith (null)

  val NumIterator = fragment ("NumIterator") {
    """local called = 0
      |local looped = 0
      |
      |local function iter(limit, n)
      |  called = called + 1
      |  if n < limit then
      |    return n + 1
      |  end
      |end
      |
      |for i in iter,10,0 do
      |  looped = looped + 1
      |end
      |
      |return called, looped
    """
  }
  NumIterator in EmptyContext succeedsWith (11, 10)

  val BasicSetList = fragment ("BasicSetList") {
    """local a = { 1, 2, 3, 4, 5 }
      |return #a, a
    """
  }
  BasicSetList in EmptyContext succeedsWith (5, classOf[Table])

  val VarLengthSetList = fragment ("VarLengthSetList") {
    """local function f() return 1, 2, 3 end
      |local a = { f(), f() }  -- should return 1, 1, 2, 3
      |return #a, a
    """
  }
  VarLengthSetList in EmptyContext succeedsWith (4, classOf[Table])

  val VarargSetList = fragment ("VarargSetList") {
    """local a = { ... }
      |return #a, a
    """
  }
  VarargSetList in EmptyContext succeedsWith (0, classOf[Table])

  // test should fail
  val GotoLocalSlot_withX = fragment ("GotoLocalSlot_withX") {
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
  GotoLocalSlot_withX in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to call a nil value")

  // test should fail, reported succeeding in Lua 5.2, 5.3
  val GotoLocalSlot_withoutX = fragment ("GotoLocalSlot_withoutX") {
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
  GotoLocalSlot_withoutX in EmptyContext failsWith (classOf[IllegalOperationAttemptException], "attempt to call a nil value")

}
