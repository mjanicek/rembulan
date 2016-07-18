package net.sandius.rembulan.parser

import net.sandius.rembulan.compiler.{ChunkClassLoader, Compiler}
import net.sandius.rembulan.core.Upvalue
import net.sandius.rembulan.core.impl.DefaultUpvalue
import net.sandius.rembulan.test.BasicFragments
import net.sandius.rembulan.{core => lua}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class NewFragmentCompileAndLoadSpec extends FunSpec with MustMatchers {

  System.setProperty("net.sandius.rembulan.compiler.VerifyAndPrint", "true")

  describe ("fragment") {

    val bundle = BasicFragments

    for (fragment <- bundle.all) {

      describe (fragment.description) {

        it ("can be compiled to Java bytecode") {
          println("-- CODE BEGIN --")
          println(fragment.code)
          println("--- CODE END ---")
          println()

          val compiler = new Compiler()
          val cm = compiler.compile(fragment.code, "stdin", "test")
          cm must not be null
        }

        it ("can be loaded by the VM") {
          val classLoader = new ChunkClassLoader()

          val compiler = new Compiler()
          val cm = compiler.compile(fragment.code, "stdin", "test")

          val name = classLoader.install(cm)
          val clazz = classLoader.loadClass(name).asInstanceOf[Class[lua.Function]]

          val f = try {
            clazz.getConstructor(classOf[Upvalue]).newInstance(new DefaultUpvalue(null))
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
