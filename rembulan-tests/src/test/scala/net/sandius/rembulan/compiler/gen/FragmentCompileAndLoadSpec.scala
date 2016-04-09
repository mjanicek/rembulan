package net.sandius.rembulan.compiler.gen

import net.sandius.rembulan.compiler.Chunk
import net.sandius.rembulan.lbc.Prototype
import net.sandius.rembulan.test.{BasicFragments, LuaCFragmentCompiler}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class FragmentCompileAndLoadSpec extends FunSpec with MustMatchers {

  describe ("fragment") {

    val loader = new LuaCFragmentCompiler("luac53")
    val bundle = BasicFragments

    for (f <- bundle.all) {

      describe (f.description) {

        var proto: Prototype = null
        var chunk: Chunk = null

        it ("can be compiled to a prototype") {
          proto = loader.compile(f)
          proto must not be null
        }

        it ("can be compiled to Java bytecode") {
          val compiler = new ChunkCompiler()
          chunk = compiler.compile(proto, "test")
          chunk must not be null
        }

        // TODO: try loading it

      }

    }

  }

}
