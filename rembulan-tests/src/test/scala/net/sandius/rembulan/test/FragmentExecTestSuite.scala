package net.sandius.rembulan.test

import java.io.PrintStream

import net.sandius.rembulan.compiler.PrototypeCompilerChunkLoader
import net.sandius.rembulan.core.PreemptionContext.AbstractPreemptionContext
import net.sandius.rembulan.core._
import net.sandius.rembulan.core.impl.DefaultLuaState
import net.sandius.rembulan.lib.LibUtils
import net.sandius.rembulan.lib.impl.{DefaultBasicLib, DefaultCoroutineLib}
import net.sandius.rembulan.parser.LuaCPrototypeReader
import net.sandius.rembulan.test.FragmentExpectations.Env
import org.scalatest.{FunSpec, MustMatchers}

import scala.util.control.NonFatal

trait FragmentExecTestSuite extends FunSpec with MustMatchers {

  def bundles: Seq[FragmentBundle]
  def expectations: Seq[FragmentExpectations]
  def contexts: Seq[FragmentExpectations.Env]

  def step: Int

  protected val Empty = FragmentExpectations.Env.Empty
  protected val Basic = FragmentExpectations.Env.Basic
  protected val Coro = FragmentExpectations.Env.Coro

  protected def envForContext(state: LuaState, ctx: Env): Table = {
    ctx match {
      case Empty => state.newTable(0, 0)
      case Basic => LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
      case Coro =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        new DefaultCoroutineLib().installInto(state, env)
        env
    }
  }

  class CountingPreemptionContext extends AbstractPreemptionContext {
    var totalCost = 0
    private var allowance = step

    override def withdraw(cost: Int): Unit = {
      totalCost += cost
      allowance -= cost
      if (allowance <= 0) {
        allowance += step
        preempt()
      }
    }
  }

  describe ("fragment") {

    val luacName = "luac53"

    for (bundle <- bundles; fragment <- bundle.all) {

      describe (fragment.description) {

        for (ctx <- contexts) {

          describe ("in " + ctx) {
            it ("can be executed") {
              val preemptionContext = new CountingPreemptionContext

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

              var steps = 0

              val before = System.nanoTime()
              val res = try {
                while (exec.isPaused) {
                  exec.resume()
                  steps += 1
                }
                Right(exec.getSink.toArray.toSeq)
              }
              catch {
                case NonFatal(ex) => Left(ex)
              }
              val after = System.nanoTime()

              val totalTimeMillis = (after - before) / 1000000.0
              val totalCPUUnitsSpent = preemptionContext.totalCost
              val avgTimePerCPUUnitNanos = (after - before).toDouble / totalCPUUnitsSpent.toDouble
              val avgCPUUnitsPerSecond = (1000000000.0 * totalCPUUnitsSpent) / (after - before)

              println("Execution took %.1f ms".format(totalTimeMillis))
              println("Total CPU cost: " + preemptionContext.totalCost + " LI")
              println("Computation steps: " + steps)
              println()
              println("Avg time per unit: %.2f ns".format(avgTimePerCPUUnitNanos))
              println("Avg units per second: %.1f LI/s".format(avgCPUUnitsPerSecond))
              println()

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
