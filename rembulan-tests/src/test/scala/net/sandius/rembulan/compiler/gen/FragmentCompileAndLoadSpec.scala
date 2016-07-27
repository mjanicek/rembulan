package net.sandius.rembulan.compiler.gen

import java.io.PrintWriter

import net.sandius.rembulan.compiler.ChunkClassLoader
import net.sandius.rembulan.core._
import net.sandius.rembulan.lbc.recompiler.Chunk
import net.sandius.rembulan.lbc.recompiler.gen.ChunkCompiler
import net.sandius.rembulan.lbc.{Prototype, PrototypePrinter}
import net.sandius.rembulan.test.{BasicFragments, LuaCFragmentCompiler}
import net.sandius.rembulan.{core => lua}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class FragmentCompileAndLoadSpec extends FunSpec with MustMatchers {

  describe ("fragment") {

    val luacName = "luac53"

    val loader = new LuaCFragmentCompiler(luacName)
    val bundle = BasicFragments

    for (fragment <- bundle.all) {

      describe (fragment.description) {

        var proto: Prototype = null
        var chunk: Chunk = null

        it ("can be compiled to a prototype") {
          proto = loader.compile(fragment)
          PrototypePrinter.print(proto, new PrintWriter(System.out))
          proto must not be null
        }

        it ("can be compiled to Java bytecode") {
          val compiler = new ChunkCompiler()
          chunk = compiler.compile(proto, "test")
          chunk must not be null
        }

        it ("can be loaded by the VM") {
          val classLoader = new ChunkClassLoader()
          val name = classLoader.install(chunk)
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
