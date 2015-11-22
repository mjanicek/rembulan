package net.sandius.rembulan.test

import net.sandius.rembulan.core._
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

    Runner.res(coro, func)
  }

}
