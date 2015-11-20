package net.sandius.rembulan.test

import net.sandius.rembulan.core._

object ByHandRunner {

  def main(args: Array[String]): Unit = {
    val st = DummyLuaState.newDummy(true)
    val inc = IncWrapper.newInc()
    val coro = st.getCurrentCoroutine
    coro.getObjectStack.push(Array(Int.box(41)))

    Runner.res(coro, inc)
  }

}
