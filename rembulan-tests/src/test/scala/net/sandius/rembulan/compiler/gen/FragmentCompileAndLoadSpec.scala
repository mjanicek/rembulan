package net.sandius.rembulan.compiler.gen

import java.io.{PrintStream, PrintWriter}

import net.sandius.rembulan.compiler.{Chunk, ChunkClassLoader, PrototypeCompilerChunkLoader}
import net.sandius.rembulan.core._
import net.sandius.rembulan.core.impl.{DefaultLuaState, PairCachingObjectSink}
import net.sandius.rembulan.lbc.{Prototype, PrototypePrinter}
import net.sandius.rembulan.lib.LibUtils
import net.sandius.rembulan.lib.impl.DefaultBasicLib
import net.sandius.rembulan.parser.LuaCPrototypeReader
import net.sandius.rembulan.test.FragmentExpectations.Env
import net.sandius.rembulan.test.FragmentExpectations.Env.{Basic, Empty}
import net.sandius.rembulan.test.{BasicFragments, LuaCFragmentCompiler}
import net.sandius.rembulan.{core => lua}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

import scala.util.control.NonFatal

@RunWith(classOf[JUnitRunner])
class FragmentCompileAndLoadSpec extends FunSpec with MustMatchers {

  describe ("fragment") {

    val luacName = "luac53"

    val loader = new LuaCFragmentCompiler(luacName)
    val bundle = BasicFragments

    for (f <- bundle.all) {

      describe (f.description) {

        var proto: Prototype = null
        var chunk: Chunk = null

        it ("can be compiled to a prototype") {
          proto = loader.compile(f)
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

          val state = new DefaultLuaState(PreemptionContext.Never.INSTANCE)
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

        def envForContext(state: LuaState, ctx: Env): Table = {
          ctx match {
            case Empty => state.newTable(0, 0)
            case Basic => LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
          }
        }

        val contexts = Seq(Env.Empty, Env.Basic)

        for (ctx <- contexts) {

          it ("can be executed in " + ctx) {
            val ldr = new PrototypeCompilerChunkLoader(new LuaCPrototypeReader(luacName), getClass.getClassLoader)

            var totalCost = 0

            val preemptionContext = new PreemptionContext.Always {
              override def withdraw(cost: Int) {
                totalCost += cost
                super.withdraw(cost)
              }
            }

            val state = new DefaultLuaState.Builder()
                .withPreemptionContext(preemptionContext)
                .build()

            val env = envForContext(state, ctx)
            val func = ldr.loadTextChunk(state.newUpvalue(env), "test", f.code)

            val res: Either[Throwable, Seq[AnyRef]] = try {
              val exec = new Exec(state)
              exec.init(func)
              while (exec.isPaused) {
                exec.resume()
              }
              Right(exec.getSink.toArray.toSeq)
            }
            catch {
              case NonFatal(ex) => Left(ex)
            }

            res match {
              case Right(result) =>
                println("Total cost: " + totalCost)
                println("Execution result (" + result.size + " values):")
                for ((v, i) <- result.zipWithIndex) {
                  println(i + ":" + "\t" + v + " (" + (if (v != null) v.getClass.getName else "null") + ")")
                }
              case _ =>
            }

            for (ctxExp <- bundle.expectationFor(f);
                 exp <- ctxExp.get(ctx)) {
              exp.tryMatch(res)(this)
            }

          }

        }


      }

    }

  }

}
