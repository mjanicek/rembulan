package net.sandius.rembulan.test

import net.sandius.rembulan.core._
import net.sandius.rembulan.{core => lua}

object Runner {

  def res(coro: Coroutine, rct: ControlThrowable): ControlThrowable = {
    try {
      val cst = rct.frames()
      cst(0).function.resume(cst, 0)
      null
    }
    catch {
      case ct: ControlThrowable =>
        inspect(coro, ct)
        ct
    }
  }

  def inspect(coro: Coroutine, ct: ControlThrowable): Unit = {
    val os = coro.getObjectStack
    var max = 0
    for (i <- 0 until os.getMaxSize) {
      if (os.get(i) != null) {
        max = i
      }
    }

    println("---")
    println("Object stack: " + (for (i <- 0 to max) yield i + ":[" + os.get(i) + "]").mkString(" "))
    println("Call stack:")
    if (ct != null) {
      val it = ct.frameIterator()
      while (it.hasNext) {
        println("\t" + it.next())
      }
    }
    else {
      println("\t(empty)")
    }
  }

  def main(args: Array[String]): Unit = {
//    val luacPath = System.getProperty("pathToLuaC")
    val luacPath = "/Users/sandius/bin/luac53"
    require (luacPath != null)

    val ploader = new LuaCPrototypeLoader(luacPath)

    val program =
      """local f = function ()
        |    return 4
        |end
        |return -1 + f() + 39
      """.stripMargin

    System.err.println(program)

    val proto = ploader.load(program)

    PrototypePrinter.print(proto, System.err)

    val name = "lua.tmp.FortyTwo"

    val loader = new PrototypeClassLoader(name)
    val mainChunk = loader.install(proto)

    val clazz = loader.findClass(mainChunk).asInstanceOf[Class[lua.Function]]
    val func = clazz.getConstructor().newInstance()

    val st = DummyLuaState.newDummy(true)
    val coro = st.getCurrentCoroutine

    val addr = coro.getObjectStack.rootView()
    var ct: ControlThrowable = new ControlThrowable {
      override def push(ci: CallInfo) = ???
      override def frameIterator() = ???
      override def last = ???
      override def frames() = Array[CallInfo](new CallInfo(func, addr, addr, 0))
    }

    LuaState.setCurrentState(st)

    do {
      ct = res(coro, ct)
    } while (ct != null)

    inspect(coro, null)

    LuaState.unsetCurrentState()

  }

}
