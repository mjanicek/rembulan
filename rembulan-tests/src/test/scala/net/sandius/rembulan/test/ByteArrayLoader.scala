package net.sandius.rembulan.test

import java.io.PrintWriter

import org.objectweb.asm.util.TraceClassVisitor
import org.objectweb.asm.ClassReader

object ByteArrayLoader extends ClassLoader {
  def defineClass(name: String, b: Array[Byte]): Class[_] = {
    System.err.println("Loading " + b.length + " bytes as \"" + name + "\"")

    System.err.println("[LISTING BEGIN]")

    val reader = new ClassReader(b)
    val cv = new TraceClassVisitor(new PrintWriter(System.err))
    reader.accept(cv, 0)

    System.err.println("[LISTING END]")

    defineClass(name, b, 0, b.length)
  }
}
