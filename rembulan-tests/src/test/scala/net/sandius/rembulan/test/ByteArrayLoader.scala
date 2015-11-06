package net.sandius.rembulan.test

object ByteArrayLoader extends ClassLoader {
  def defineClass(name: String, b: Array[Byte]): Class[_] = defineClass(name, b, 0, b.length)
}
