package net.sandius.rembulan.test

import net.sandius.rembulan.core._
import net.sandius.rembulan.{core => lua}

object CoroutineFragments extends FragmentBundle with FragmentExpectations  {

  val CreateReturnsAThread = fragment ("CreateReturnsAThread") {
    """f = function(...)
      |  return ...
      |end
      |
      |return coroutine.create(f)
    """
  }
  CreateReturnsAThread in CoroContext succeedsWith (classOf[Coroutine])

  val ResumeReturnsControlToCallerOnExit = fragment ("ResumeReturnsControlToCallerOnExit") {
    """t = coroutine.create(function() end)
      |return coroutine.resume(t)
    """
  }
  ResumeReturnsControlToCallerOnExit in CoroContext succeedsWith (true)

  val ResumeReturnsValuesOnExit = fragment ("ResumeReturnsValuesOnExit") {
    """f = function(...)
      |  return ...
      |end
      |
      |t = coroutine.create(f)
      |return coroutine.resume(t, 'Yes', false, nil, 0)
    """
  }
  ResumeReturnsValuesOnExit in CoroContext succeedsWith (true, "Yes", false, null, 0)

  val CoroutineIsDeadOnceItReturns = fragment ("CoroutineIsDeadOnceItReturns") {
    """t = coroutine.create(function() end)
      |s0 = coroutine.status(t)
      |coroutine.resume(t)
      |s1 = coroutine.status(t)
      |return s0, s1
    """
  }
  CoroutineIsDeadOnceItReturns in CoroContext succeedsWith ("suspended", "dead")

  val RunningReturnsACoroutine = fragment ("RunningReturnsACoroutine") {
    """local c = coroutine.running()
      |return c
    """
  }
  RunningReturnsACoroutine in CoroContext succeedsWith (classOf[Coroutine])

  val MainCoroutineIsNotYieldable = fragment ("MainCoroutineIsNotYieldable") {
    """local y = coroutine.isyieldable()
      |return y
    """
  }
  MainCoroutineIsNotYieldable in CoroContext succeedsWith (false)

  val WrapReturnsAFunction = fragment ("WrapReturnsAFunction") {
    """local function f(...) return ... end
      |local w = coroutine.wrap(f)
      |return w
    """
  }
  WrapReturnsAFunction in CoroContext succeedsWith (classOf[lua.Function])

  val RunningCoroutineStatus = fragment ("RunningCoroutineStatus") {
    """local c = coroutine.running()
      |local s = coroutine.status(c)
      |return s
    """
  }
  RunningCoroutineStatus in CoroContext succeedsWith ("running")

  val YieldFromOutsideCoroutine = fragment ("YieldFromOutsideCoroutine") {
    """coroutine.yield()
      |return
    """
  }
  YieldFromOutsideCoroutine in CoroContext failsWith (classOf[IllegalOperationAttemptException], "attempt to yield from outside a coroutine")

}
