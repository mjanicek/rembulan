package net.sandius.rembulan.test

import java.io.PrintStream

import net.sandius.rembulan.compiler.PrototypeCompilerChunkLoader
import net.sandius.rembulan.core._
import net.sandius.rembulan.core.impl.DefaultLuaState
import net.sandius.rembulan.lib.LibUtils
import net.sandius.rembulan.lib.impl.DefaultBasicLib
import net.sandius.rembulan.parser.LuaCPrototypeReader
import net.sandius.rembulan.test.FragmentExpectations.Env
import org.scalatest.{FunSpec, MustMatchers}

import scala.util.control.NonFatal

trait FragmentExecTestSuite extends FunSpec with MustMatchers {

  def bundles: Seq[FragmentBundle]
  def expectations: Seq[FragmentExpectations]
  def contexts: Seq[FragmentExpectations.Env]

  protected val Empty = FragmentExpectations.Env.Empty
  protected val Basic = FragmentExpectations.Env.Basic

  protected def envForContext(state: LuaState, ctx: Env): Table = {
    ctx match {
      case Empty => state.newTable(0, 0)
      case Basic => LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
    }
  }

  class CountingAlwaysPreemptingPreemptionContext extends PreemptionContext.Always {
    var totalCost = 0
    override def withdraw(cost: Int) {
      totalCost += cost
      super.withdraw(cost)
    }
  }

  describe ("fragment") {

    val luacName = "luac53"

    for (bundle <- bundles; fragment <- bundle.all) {

      describe (fragment.description) {

        for (ctx <- contexts) {

          describe ("in " + ctx) {
            it ("can be executed") {
              val preemptionContext = new CountingAlwaysPreemptingPreemptionContext

              val exec = Util.timed("Compilation and setup") {
                val ldr = new PrototypeCompilerChunkLoader(
                  new LuaCPrototypeReader(luacName),
                  getClass.getClassLoader)


                val state = new DefaultLuaState.Builder()
                    .withPreemptionContext(preemptionContext)
                    .build()

                val env = envForContext(state, ctx)
                val func = ldr.loadTextChunk(state.newUpvalue(env), "test", fragment.code)

                val exec = new Exec(state)
                exec.init(func)
                exec
              }

              val res = Util.timed("Execution") {
                try {
                  while (exec.isPaused) {
                    exec.resume()
                  }
                  Right(exec.getSink.toArray.toSeq)
                }
                catch {
                  case NonFatal(ex) => Left(ex)
                }
              }

              println("Total CPU cost: " + preemptionContext.totalCost)

              res match {
                case Right(result) =>
                  println("Result: success (" + result.size + " values):")
                  for ((v, i) <- result.zipWithIndex) {
                    println(i + ":" + "\t" + v + " (" + (if (v != null) v.getClass.getName else "null") + ")")
                  }
                case Left(ex) =>
                  println("Result: error: " + ex.getMessage)
              }

              for (expects <- expectations;
                    ctxExp <- expects.expectationFor(fragment);
                    exp <- ctxExp.get(ctx)) {

                exp.tryMatch(res)(this)

              }
            }
          }

        }
      }
    }

  }

}
