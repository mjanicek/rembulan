package net.sandius.rembulan.test

import net.sandius.rembulan.lbc.Prototype
import net.sandius.rembulan.parser.LuaCPrototypeLoader

trait FragmentCompiler {

  def version: String

  def compile(fragment: Fragment): Prototype

}

class LuaCFragmentCompiler(pathToLuaC: String) extends FragmentCompiler {

  require (pathToLuaC != null)

  val ploader = new LuaCPrototypeLoader(pathToLuaC)

  override def version = ploader.getVersion

  override def compile(fragment: Fragment) = ploader.load(fragment.code)

}
