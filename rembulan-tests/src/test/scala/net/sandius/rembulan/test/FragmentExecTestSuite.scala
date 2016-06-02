package net.sandius.rembulan.test

import java.io.PrintStream

import net.sandius.rembulan.compiler.PrototypeCompilerChunkLoader
import net.sandius.rembulan.core.PreemptionContext.AbstractPreemptionContext
import net.sandius.rembulan.core._
import net.sandius.rembulan.core.impl.DefaultLuaState
import net.sandius.rembulan.lib.LibUtils
import net.sandius.rembulan.lib.impl._
import net.sandius.rembulan.parser.LuaCPrototypeReader
import net.sandius.rembulan.test.FragmentExpectations.Env
import net.sandius.rembulan.{core => lua}
import org.scalatest.{FunSpec, MustMatchers}

trait FragmentExecTestSuite extends FunSpec with MustMatchers {

  import FragmentExecTestSuite._

  def bundles: Seq[FragmentBundle]
  def expectations: Seq[FragmentExpectations]
  def contexts: Seq[FragmentExpectations.Env]

  def steps: Seq[Int]

  protected val Empty = FragmentExpectations.Env.Empty
  protected val Basic = FragmentExpectations.Env.Basic
  protected val Coro = FragmentExpectations.Env.Coro
  protected val Math = FragmentExpectations.Env.Math
  protected val Str = FragmentExpectations.Env.Str
  protected val IO = FragmentExpectations.Env.IO

  protected def envForContext(state: LuaState, ctx: Env): Table = {
    ctx match {
      case Empty => state.newTable()

      case Basic => LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))

      case Coro =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        val coro = state.newTable()
        new DefaultCoroutineLib().installInto(state, coro)
        env.rawset("coroutine", coro)
        env

      case Math =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        val mathlib = state.newTable()
        new DefaultMathLib().installInto(state, mathlib)
        env.rawset("math", mathlib)
        env

      case Str =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        val str = state.newTable()
        new DefaultStringLib().installInto(state, str)
        env.rawset("string", str)
        env

      case IO =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        val io = state.newTable()
        new DefaultIOLib(state).installInto(state, io)
        env.rawset("io", io)
        env

    }
  }

  describe ("fragment") {

    val luacName = "luac53"

    for (bundle <- bundles; fragment <- bundle.all) {

      describe (fragment.description) {

        for (ctx <- contexts) {

          for (s <- steps) {

            describe ("in " + ctx) {
              it ("can be executed with " + s + " steps") {
                val preemptionContext = new CountingPreemptionContext()

                val exec = Util.timed("Compilation and setup") {
                  val ldr = new PrototypeCompilerChunkLoader(
                    new LuaCPrototypeReader(luacName),
                    getClass.getClassLoader)


                  val state = new DefaultLuaState.Builder()
                      .withPreemptionContext(preemptionContext)
                      .build()

                  val env = envForContext(state, ctx)
                  val func = ldr.loadTextChunk(state.newUpvalue(env), "test", fragment.code)

                  new Exec(state, func)
                }

                var steps = 0

                val before = System.nanoTime()

                var execState = exec.getExecutionState
                while (exec.isPaused) {
                  preemptionContext.deposit(s)
                  if (preemptionContext.allowed) {
                    execState = exec.resume()
                  }
                  steps += 1
                }

                val res = execState match {
                  case es: ExecutionState.TerminatedAbnormally => Left(es.getError)
                  case _ => Right(exec.getSink.toArray.toSeq)
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

                println("Result state: " + execState)
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

}

object FragmentExecTestSuite {

  class CountingPreemptionContext extends AbstractPreemptionContext {
    var totalCost = 0L
    private var allowance = 0L

    override def withdraw(cost: Int): Preemption = {
      totalCost += cost
      allowance -= cost
      if (!allowed) preempt() else null
    }

    def deposit(n: Int): Unit = {
      allowance += n
    }

    def allowed = allowance > 0

  }

}
