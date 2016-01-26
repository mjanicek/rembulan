package net.sandius.rembulan.test

trait Fragment {

  def description: Option[String] = Some(this.getClass.getCanonicalName)

  private var _code: String = null

  def code: String = _code

  protected def code_=(v: String): Unit = {
    require (v != null)
    this._code = v.stripMargin
  }

}

object Fragment {

  class DefaultImpl(_desc: String, _code: String) extends Fragment {
    require (_code != null)

    override val description = Option(_desc)
    override val code = _code.stripMargin
  }

  def apply(description: String, code: String): Fragment = new DefaultImpl(description, code)

  def apply(code: String): Fragment = apply(null, code)

}
