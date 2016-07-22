package net.sandius.rembulan.test

import java.io.PrintStream

import net.sandius.rembulan.compiler.{ChunkClassLoader, CompilerChunkLoader}
import net.sandius.rembulan.core.PreemptionContext.AbstractPreemptionContext
import net.sandius.rembulan.core._
import net.sandius.rembulan.core.impl.DefaultLuaState
import net.sandius.rembulan.lbc.LuaCPrototypeReader
import net.sandius.rembulan.lbc.recompiler.PrototypeCompilerChunkLoader
import net.sandius.rembulan.lib.{Lib, LibUtils}
import net.sandius.rembulan.lib.impl._
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
  protected val Tab = FragmentExpectations.Env.Tab
  protected val Debug = FragmentExpectations.Env.Debug
  protected val Full = FragmentExpectations.Env.Full

  def installLib(state: LuaState, env: Table, name: String, impl: Lib): Unit = {
    val lib = state.newTable()
    impl.installInto(state, lib)
    env.rawset(name, lib)
  }

  protected def envForContext(state: LuaState, ctx: Env): Table = {
    ctx match {
      case Empty => state.newTable()

      case Basic => LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))

      case Coro =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        installLib(state, env, "coroutine", new DefaultCoroutineLib())
        env

      case Math =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        installLib(state, env, "math", new DefaultMathLib())
        env

      case Str =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        installLib(state, env, "string", new DefaultStringLib())
        env

      case IO =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        installLib(state, env, "io", new DefaultIOLib(state))
        env

      case Tab =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        installLib(state, env, "table", new DefaultTableLib())
        env

      case Debug =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        installLib(state, env, "debug", new DefaultDebugLib())
        env

      case Full =>
        val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
        // TODO: module lib!
        installLib(state, env, "coroutine", new DefaultCoroutineLib())
        installLib(state, env, "math", new DefaultMathLib())
        installLib(state, env, "string", new DefaultStringLib())
        installLib(state, env, "io", new DefaultIOLib(state))
        installLib(state, env, "table", new DefaultTableLib())
        installLib(state, env, "debug", new DefaultDebugLib())
        env

    }
  }

  sealed trait ChkLoader {
    def name: String
    def loader(): ChunkLoader
  }

  case object LuacChkLoader extends ChkLoader {
    val luacName = "luac53"
    def name = "LuaC"
    def loader() = new PrototypeCompilerChunkLoader(
      new LuaCPrototypeReader(luacName),
      getClass.getClassLoader)
  }

  case object RembulanChkLoader extends ChkLoader {
    def name = "RemC"
    def loader() = new CompilerChunkLoader(new ChunkClassLoader())
  }

  val ldrs = Seq(LuacChkLoader, RembulanChkLoader)

  for (bundle <- bundles;
       fragment <- bundle.all;
       ctx <- contexts) {

    val prefix = ""

    describe (prefix + fragment.description + " in " + ctx + ":") {

      for (s <- steps; l <- ldrs) {

        val stepDesc = s match {
          case Int.MaxValue => "max"
          case i => i.toString
        }

        it (l.name + " / " + stepDesc) {

          val preemptionContext = new CountingPreemptionContext()

          val exec = Util.timed("Compilation and setup") {

            val ldr = l.loader()

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
                println(i + ":" + "\t" + Conversions.toHumanReadableString(v) + " (" + (if (v != null) v.getClass.getName else "null") + ")")
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

object FragmentExecTestSuite {

  class CountingPreemptionContext extends AbstractPreemptionContext {
    var totalCost = 0L
    private var allowance = 0L

    override def withdraw(cost: Int): Unit = {
      totalCost += cost
      allowance -= cost
      if (!allowed) {
        preempt()
      }
    }

    def deposit(n: Int): Unit = {
      allowance += n
    }

    def allowed = allowance > 0

  }

}
