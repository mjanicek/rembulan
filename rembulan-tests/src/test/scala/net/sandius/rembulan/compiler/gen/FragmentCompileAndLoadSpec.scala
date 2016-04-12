package net.sandius.rembulan.compiler.gen

import net.sandius.rembulan.compiler.{Chunk, ChunkClassLoader}
import net.sandius.rembulan.core.{Dispatch, Upvalue}
import net.sandius.rembulan.core.impl.PairCachingObjectSink
import net.sandius.rembulan.lbc.Prototype
import net.sandius.rembulan.test.{BasicFragments, DummyLuaState, FragmentExpectations, LuaCFragmentCompiler}
import net.sandius.rembulan.{core => lua}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

import scala.util.control.NonFatal

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

        it ("can be loaded by the VM") {
          val classLoader = new ChunkClassLoader()
          val name = classLoader.install(chunk)
          val clazz = classLoader.loadClass(name).asInstanceOf[Class[lua.Function]]

          val state = new DummyLuaState(false)
          val os = new PairCachingObjectSink

          val env = state.newTable(0, 0)
          val upEnv = state.newUpvalue(env)

          val f = try {
            clazz.getConstructor(classOf[Upvalue]).newInstance(upEnv)
          }
          catch {
            case ex: VerifyError => throw new IllegalStateException(ex)
          }

          f must not be null
        }

        it ("can be executed") {
          val classLoader = new ChunkClassLoader()
          val name = classLoader.install(chunk)
          val clazz = classLoader.loadClass(name).asInstanceOf[Class[lua.Function]]

          val state = new DummyLuaState(false)
          val os = new PairCachingObjectSink

          val env = state.newTable(0, 0)
          val upEnv = state.newUpvalue(env)

          val res: Either[Throwable, Seq[AnyRef]] = try {
            val f = clazz.getConstructor(classOf[Upvalue]).newInstance(upEnv)

            Dispatch.call(state, os, f)

            val result = os.toArray.toSeq
            println("Execution result (" + result.size + " values):")
            for ((v, i) <- result.zipWithIndex) {
              println(i + ":" + "\t" + v + " (" + (if (v != null) v.getClass.getName else "null") + ")")
            }
            Right(result)
          }
          catch {
            case ex: VerifyError => throw new IllegalStateException(ex)
            case ex: NoSuchMethodError => throw new IllegalStateException(ex)
            case NonFatal(ex) => Left(ex)
          }

          for (ctxExp <- bundle.expectationFor(f);
               exp <- ctxExp.get(FragmentExpectations.Env.Empty)) {
            exp.tryMatch(res)(this)
          }

        }

      }

    }

  }

}
