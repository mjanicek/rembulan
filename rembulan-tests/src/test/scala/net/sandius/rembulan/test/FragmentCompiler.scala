package net.sandius.rembulan.test

import net.sandius.rembulan.lbc.{LuaCPrototypeReader, Prototype}

trait FragmentCompiler {

  def version: String

  def compile(fragment: Fragment): Prototype

}

class LuaCFragmentCompiler(pathToLuaC: String) extends FragmentCompiler {

  require (pathToLuaC != null)

  val ploader = new LuaCPrototypeReader(pathToLuaC)

  override def version = ploader.getVersion

  override def compile(fragment: Fragment) = ploader.load(fragment.code)

}
