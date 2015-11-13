package net.sandius.rembulan.test

import net.sandius.rembulan.core._

object IncWrapper {

  def incClazzForName(name: String) = ByteArrayLoader.defineClass(name, new IncCallInfoDump(name).dump())

  private lazy val incClazz = incClazzForName("net.sandius.rembulan.test.IncFunction")

  def newInc(state: LuaState): Function = {
    val constructor = incClazz.getConstructor(classOf[LuaState], Integer.TYPE)
    constructor.newInstance(state, Int.box(0)).asInstanceOf[Function]
  }

}
