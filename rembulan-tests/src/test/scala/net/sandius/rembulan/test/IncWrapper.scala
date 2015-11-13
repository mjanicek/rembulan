package net.sandius.rembulan.test

import net.sandius.rembulan.core._

object IncWrapper {

  def incClazzForName(name: String) = ByteArrayLoader.defineClass(name, new IncCallInfoDump(name).dump())

  private lazy val incClazz = incClazzForName("net.sandius.rembulan.test.IncCallInfo")

  def newInc(ctx: LuaState, objectStack: ObjectStack): CallInfo = {
    val constructor = incClazz.getConstructor(classOf[LuaState], classOf[ObjectStack], Integer.TYPE)
    constructor.newInstance(ctx, objectStack, Int.box(0)).asInstanceOf[CallInfo]
  }

}
