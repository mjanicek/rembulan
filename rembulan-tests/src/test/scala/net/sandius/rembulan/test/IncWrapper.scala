package net.sandius.rembulan.test

import net.sandius.rembulan.core._

object IncWrapper {

  def incClazzForName(name: String) = ByteArrayLoader.defineClass(name, new IncCallInfoDump(name).dump())

  private lazy val incClazz = incClazzForName("net.sandius.rembulan.test.IncCallInfo")

  def newInc(ctx: PreemptionContext): CallInfo = {
    val constructor = incClazz.getConstructor(classOf[PreemptionContext], Integer.TYPE)
    constructor.newInstance(ctx, Int.box(10)).asInstanceOf[CallInfo]
  }

}
