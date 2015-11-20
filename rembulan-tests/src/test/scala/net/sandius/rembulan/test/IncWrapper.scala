package net.sandius.rembulan.test

import net.sandius.rembulan.core._

object IncWrapper {

  def incClazzForName(name: String) = ByteArrayLoader.defineClass(name, new IncCallInfoDump(name).dump())

  private lazy val incClazz = incClazzForName("lua.tmp.FortyTwo")

  def newInc(): Function = incClazz.getConstructor().newInstance().asInstanceOf[Function]

}
