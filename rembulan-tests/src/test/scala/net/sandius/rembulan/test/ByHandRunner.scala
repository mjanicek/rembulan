package net.sandius.rembulan.test

import net.sandius.rembulan.core._

object ByHandRunner {

  def main(args: Array[String]): Unit = {
//    val st = DummyLuaState.newDummy(true)
//    val inc = IncWrapper.newInc()
//    val coro = st.getCurrentCoroutine
//    coro.getObjectStack.push(Array(Int.box(41)))

//    Runner.res(coro, inc)

    val f0 = new UnaryMinus()
    val f1 = new PlusOne()
    val f2 = new MinusPlus()

    Runner.doRun(f2, f0, f1)

//    val func = new Example()
//    Runner.doRun(func, 1:Integer, 42:Integer)

  }

}
