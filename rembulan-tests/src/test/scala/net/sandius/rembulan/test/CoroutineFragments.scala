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

  val ResumeReturnsControlToCallerOnExit1 = fragment ("ResumeReturnsControlToCallerOnExit1") {
    """t = coroutine.create(function() end)
      |coroutine.resume(t)
      |assert(false)
    """
  }
  ResumeReturnsControlToCallerOnExit1 in CoroContext failsWith (classOf[IllegalStateException], "assertion failed!")

  val ResumeReturnsControlToCallerOnExit2 = fragment ("ResumeReturnsControlToCallerOnExit2") {
    """return coroutine.resume(coroutine.create(function() end))
    """
  }
  ResumeReturnsControlToCallerOnExit2 in CoroContext succeedsWith (true)

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

  val NewCoroutineIsYieldable = fragment ("NewCoroutineIsYieldable") {
    """return coroutine.resume(coroutine.create(coroutine.isyieldable))
    """
  }
  NewCoroutineIsYieldable in CoroContext succeedsWith (true, true)

  val ResumeCurrentFails = fragment ("ResumeCurrentFails") {
    """return coroutine.resume(coroutine.running())
    """
  }
  ResumeCurrentFails in CoroContext succeedsWith (false, "cannot resume non-suspended coroutine")

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
