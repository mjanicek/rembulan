package net.sandius.rembulan.test

import net.sandius.rembulan.core.{PrototypeCompiler, LuaCPrototypeLoader, Coroutine, ControlThrowable}
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

    val proto = ploader.load("local y = 2\nreturn y + 40", "fortytwo.lua")

    val name = "lua.tmp.FortyTwo"

    val bytecode = PrototypeCompiler.compile(proto, name)
    val clazz = ByteArrayLoader.defineClass(name, bytecode)
    val func = clazz.getConstructor().newInstance().asInstanceOf[lua.Function]

    val st = DummyLuaState.newDummy(true)
    val coro = st.getCurrentCoroutine

    Runner.res(coro, func)
  }

}
