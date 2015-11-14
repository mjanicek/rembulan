package net.sandius.rembulan.test

import net.sandius.rembulan.core.{Coroutine, ControlThrowable}
import net.sandius.rembulan.{core => lua}

object Runner {

  def res(coro: Coroutine, f: lua.Function): Unit = {
    try {
      f.resume(coro, 0, 0, 0)
    }
    catch {
      case ct: ControlThrowable =>
        val it = ct.frameIterator()
        while (it.hasNext) {
          println(it.next())
        }
    }
  }

  def main(args: Array[String]): Unit = {
    val st = DummyLuaState.newDummy(true)
    val inc = IncWrapper.newInc()
    val coro = st.getCurrentCoroutine
    coro.getObjectStack.push(Array(Int.box(41)))

    Runner.res(coro, inc)
  }

}
