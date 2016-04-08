package net.sandius.rembulan.test

import scala.collection.mutable.ArrayBuffer

trait Fragment {

  def description: String

  private var _code: String = null

  def code: String = _code

  protected def code_=(v: String): Unit = {
    require (v != null)
    this._code = v.stripMargin
  }

}

object Fragment {

  class DefaultImpl(_desc: String, _code: String) extends Fragment {
    require (_desc != null)
    require (_code != null)

    override val description = _desc
    override val code = _code.stripMargin
  }

  def apply(description: String, code: String): Fragment = new DefaultImpl(description, code)

}

trait FragmentBundle {

  implicit protected val bundle = this

  private val fragments = ArrayBuffer.empty[Fragment]

  protected def register(fragment: Fragment): Fragment = {
    fragments.append(fragment)
    fragment
  }

  def lookup(name: String): Option[Fragment] = fragments find { _.description == name }

  def all: Iterable[Fragment] = fragments

  protected def fragment(name: String)(body: String): Fragment = {
    register(Fragment(name, body))
  }

}
