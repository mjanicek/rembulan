package net.sandius.rembulan.parser

import net.sandius.rembulan.compiler.{ChunkClassLoader, Compiler}
import net.sandius.rembulan.core.Variable
import net.sandius.rembulan.test.BasicFragments
import net.sandius.rembulan.{core => lua}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class NewFragmentCompileAndLoadSpec extends FunSpec with MustMatchers {

  def withProperty[A](key: String, value: String)(body: => A): A = {
    val oldValue = System.getProperty(key)
    try {
      System.setProperty(key, value)
      body
    }
    finally {
      if (oldValue == null) {
        System.clearProperty(key)
      }
      else {
        System.setProperty(key, oldValue)
      }
    }
  }

  describe ("fragment") {

    val bundle = BasicFragments

    for (fragment <- bundle.all) {

      describe (fragment.description) {

        it ("can be compiled to Java bytecode") {
          println("-- CODE BEGIN --")
          println(fragment.code)
          println("--- CODE END ---")
          println()

          withProperty("net.sandius.rembulan.compiler.VerifyAndPrint", "true") {
            val compiler = new Compiler()
            val cm = compiler.compile(fragment.code, "stdin", "test")
            cm must not be null
          }
        }

        it ("can be loaded by the VM") {
          val classLoader = new ChunkClassLoader()

          val cm = withProperty("net.sandius.rembulan.compiler.VerifyAndPrint", "true") {
            val compiler = new Compiler()
            compiler.compile(fragment.code, "stdin", "test")
          }

          val name = classLoader.install(cm)
          val clazz = classLoader.loadClass(name).asInstanceOf[Class[lua.Function]]

          val f = try {
            clazz.getConstructor(classOf[Variable]).newInstance(new Variable(null))
          }
          catch {
            case ex: VerifyError => throw new IllegalStateException(ex)
          }

          f must not be null
        }

      }
    }
  }

}
