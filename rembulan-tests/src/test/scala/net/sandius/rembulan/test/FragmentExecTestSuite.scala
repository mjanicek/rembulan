/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.test

import java.io.PrintStream

import net.sandius.rembulan.compiler.CompilerSettings.CPUAccountingMode
import net.sandius.rembulan.compiler.{ChunkClassLoader, CompilerChunkLoader, CompilerSettings}
import net.sandius.rembulan.core.Call.EventHandler
import net.sandius.rembulan.core._
import net.sandius.rembulan.core.impl.DefaultLuaState
import net.sandius.rembulan.lib.impl._
import net.sandius.rembulan.lib.{Lib, LibUtils}
import net.sandius.rembulan.test.FragmentExpectations.Env
import net.sandius.rembulan.{core => lua}
import org.scalatest.{FunSpec, MustMatchers}

import scala.concurrent.Promise
import scala.util.{Failure, Success}

trait FragmentExecTestSuite extends FunSpec with MustMatchers {

  import FragmentExecTestSuite._

  def bundles: Seq[FragmentBundle]
  def expectations: Seq[FragmentExpectations]
  def contexts: Seq[FragmentExpectations.Env]

  def steps: Seq[Int]

  def compilerConfigs: CompilerConfigs = CompilerConfigs.DefaultOnly

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

  def compilerSettingsToString(settings: CompilerSettings): String = {
    val cpu = settings.cpuAccountingMode() match {
      case CPUAccountingMode.NO_CPU_ACCOUNTING => "n"
      case CPUAccountingMode.IN_EVERY_BASIC_BLOCK => "a"
    }
    val cfold = settings.constFolding() match {
      case true => "t"
      case false => "f"
    }
    val ccache = settings.constCaching() match {
      case true => "t"
      case false => "f"
    }
    cpu + cfold + ccache
  }

  case class RembulanChkLoader(settings: CompilerSettings) extends ChkLoader {
    def name = "RemC" + "_" + compilerSettingsToString(settings)
    def loader() = new CompilerChunkLoader(new ChunkClassLoader(), settings)
  }

  class CompilerConfigs private (configs: Seq[CompilerSettings]) {
    def loaders: Seq[RembulanChkLoader] = configs.distinct map RembulanChkLoader
  }
  object CompilerConfigs {
    val bools = Seq(true, false)

    val allConfigs = for (
      cpu <- CPUAccountingMode.values();
      cfold <- bools;
      ccache <- bools
    ) yield CompilerSettings.defaultSettings()
        .withCPUAccountingMode(cpu)
        .withConstFolding(cfold)
        .withConstCaching(ccache)

    case object DefaultOnly extends CompilerConfigs(Seq(CompilerSettings.defaultSettings()))
    case object All extends CompilerConfigs(allConfigs)
  }

  val ldrs = compilerConfigs.loaders

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

          val resultPromise = Promise[Array[AnyRef]]()

          val handler = new EventHandler {
            override def paused(c: Call, cont: Call.Continuation, pc: PreemptionContext) { }
            override def waiting(c: Call, task: Runnable, cont: Call.Continuation, pc: PreemptionContext) = {
              task.run()
            }
            override def returned(c: Call, result: Array[AnyRef]) = resultPromise.success(result)
            override def failed(c: Call, error: Throwable) = resultPromise.failure(error)
          }

          val exec = Util.timed("Compilation and setup") {

            val ldr = l.loader()

            val state = new DefaultLuaState()

            val env = envForContext(state, ctx)
            val func = ldr.loadTextChunk(new Variable(env), "test", fragment.code)

            Call.init(state, func)
          }

          var steps = 0

          val before = System.nanoTime()

          while (exec.state() == Call.State.PAUSED) {
            preemptionContext.deposit(s)
            if (preemptionContext.allowed) {
              exec.resume(handler, preemptionContext)
            }
            steps += 1
          }

          val res = resultPromise.future.value match {
            case Some(Success(vs)) => Success(vs.toSeq)
            case Some(Failure(ex)) => Failure(ex);
            case None => Failure(null)
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
            case Success(result) =>
              println("Result: success (" + result.size + " values):")
              for ((v, i) <- result.zipWithIndex) {
                println(i + ":" + "\t" + Conversions.toHumanReadableString(v) + " (" + (if (v != null) v.getClass.getName else "null") + ")")
              }
            case Failure(ex) =>
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

  class CountingPreemptionContext extends PreemptionContext {
    var totalCost = 0L
    private var allowance = 0L

    override def withdraw(cost: Int) = {
      totalCost += cost
      allowance -= cost
      isPreempted
    }

    override def isPreempted = allowance <= 0

    def deposit(n: Int): Unit = {
      allowance += n
    }

    def allowed = allowance > 0

  }

}
