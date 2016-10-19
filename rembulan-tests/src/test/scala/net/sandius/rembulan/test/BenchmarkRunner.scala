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

import java.util.Scanner

import net.sandius.rembulan.compiler.CompilerSettings.CPUAccountingMode
import net.sandius.rembulan.compiler.{CompilerChunkLoader, CompilerSettings}
import net.sandius.rembulan.env.RuntimeEnvironments
import net.sandius.rembulan.exec.DirectCallExecutor
import net.sandius.rembulan.impl.StateContexts
import net.sandius.rembulan.lib.impl._
import net.sandius.rembulan.load.{ChunkClassLoader, ChunkLoader}
import net.sandius.rembulan.runtime.LuaFunction
import net.sandius.rembulan.{StateContext, Table, Variable}

import scala.util.Try

object BenchmarkRunner {

  case class RequestedCompilerSettings(
      noCPUAccounting: Boolean,
      constFolding: Option[Boolean],
      constCaching: Option[Boolean]
  ) {

    def toCompilerSettings: CompilerSettings = {
      val s0 = CompilerSettings.defaultSettings()

      val s1 = if (noCPUAccounting) s0.withCPUAccountingMode(CompilerSettings.CPUAccountingMode.NO_CPU_ACCOUNTING) else s0

      val s2 = constFolding match {
        case Some(v) => s1.withConstFolding(v)
        case _ => s1
      }

      val s3 = constCaching match {
        case Some(v) => s2.withConstCaching(v)
        case _ => s2
      }

      s3
    }

  }

  case class Benchmark(fileName: String) {
    def go(prefix: String, stepSize: Int, settings: CompilerSettings, args: String*): Unit = {
      doFile(prefix, stepSize, settings, fileName, args:_*)
    }
  }

  protected def stringProperty(key: String, default: String): String = {
    Option(System.getProperty(key)) getOrElse default
  }

  protected def intProperty(key: String, default: Int): Int = {
    Option(System.getProperty(key)) flatMap { s => Try(s.toInt).toOption } getOrElse default
  }

  protected def booleanProperty(key: String, default: Boolean): Boolean = {
    Option(System.getProperty(key)) match {
      case Some("true") => true
      case _ => false
    }
  }

  protected def optBooleanProperty(key: String): Option[Boolean] = {
    Option(System.getProperty(key)) match {
      case Some("true") => Some(true)
      case Some("false") => Some(false)
      case _ => None
    }
  }

  def initEnv(context: StateContext, loader: ChunkLoader, args: Seq[String]): Table = {
    val runtimeEnv = RuntimeEnvironments.system()
    val env = context.newTable()

    DefaultBasicLib.installInto(context, env, runtimeEnv, loader)
    DefaultModuleLib.installInto(context, env, runtimeEnv, loader, getClass.getClassLoader)
    DefaultCoroutineLib.installInto(context, env)
    DefaultMathLib.installInto(context, env)
    DefaultStringLib.installInto(context, env)
    DefaultIoLib.installInto(context, env, runtimeEnv)
    DefaultOsLib.installInto(context, env, runtimeEnv)
    DefaultUtf8Lib.installInto(context, env)
    DefaultTableLib.installInto(context, env)
    DefaultDebugLib.installInto(context, env)

    // command-line arguments
    val argTable = context.newTable()
    for ((a, i) <- args.zipWithIndex) {
      argTable.rawset(i + 1, a)
    }
    env.rawset("arg", argTable)

    env
  }

  case class EnvWithMainChunk(state: StateContext, fn: LuaFunction)

  def init(settings: CompilerSettings, filename: String, args: String*) = {
    val resourceStream = getClass.getResourceAsStream(filename)
    require (resourceStream != null, "resource must exist, is null")
    val sourceContents = new Scanner(resourceStream, "UTF-8").useDelimiter("\\A").next()
    require (sourceContents != null, "source contents must not be null")

    val ldr = CompilerChunkLoader.of(new ChunkClassLoader(), settings, "benchmark_")

    val state = StateContexts.newDefaultInstance()

    val env = initEnv(state, ldr, args)

    val func = ldr.loadTextChunk(new Variable(env), "benchmarkMain", sourceContents)

    EnvWithMainChunk(state, func)
  }

  def timed[A](name: String)(body: => A): A = {
    val before = System.nanoTime()
    val result = body
    val after = System.nanoTime()

    val totalTimeMillis = (after - before) / 1000000.0
    println("%s took %.1f ms".format(name, totalTimeMillis))
    result
  }

  def doFile(prefix: String, stepSize: Int, settings: CompilerSettings, filename: String, args: String*): Unit = {

    def initCall() = timed (prefix + "init") {
      init(settings, filename, args:_*)
    }

    val c = initCall()


    val executor = DirectCallExecutor.newExecutor()

    val before = System.nanoTime()
    executor.call(c.state, c.fn)
    val after = System.nanoTime()

    val totalTimeMillis = (after - before) / 1000000.0
//    val totalCPUUnitsSpent = pc.totalCost
//    val avgTimePerCPUUnitNanos = (after - before).toDouble / totalCPUUnitsSpent.toDouble
//    val avgCPUUnitsPerSecond = (1000000000.0 * totalCPUUnitsSpent) / (after - before)

    println(prefix + "Execution took %.1f ms".format(totalTimeMillis))
    if (settings.cpuAccountingMode() != CPUAccountingMode.NO_CPU_ACCOUNTING) {
//      println(prefix + "Total CPU cost: " + pc.totalCost + " LI")
//      println(prefix)
//      println(prefix + "Step size: " + stepSize + " LI")
//      println(prefix + "Num of steps: " + steps)
//      println(prefix + "Avg time per step: %.3f ms".format(totalTimeMillis / steps))
//      println(prefix)
//      println(prefix + "Avg time per unit: %.2f ns".format(avgTimePerCPUUnitNanos))
//      println(prefix + "Avg units per second: %.1f LI/s".format(avgCPUUnitsPerSecond))
    }
    println()
  }

  val dirPrefix = "/benchmarksgame/"

  private case class Setup(benchmarkFile: String, args: Seq[String]) {

  }

  private def getSetup(args: Array[String]): Option[Setup] = {
    args.toList match {
      case fileName :: tail => Some(Setup(fileName, tail))
      case _ => None
    }
  }

  val NumOfRunsPropertyName = "numRuns"
  val DefaultNumOfRuns = 3

  val StepSizePropertyName = "stepSize"
  val DefaultStepSize = 1000000

  val NoCPUAccountingPropertyName = "noCPUAccounting"
  val DefaultNoCPUAccounting = false

  val ConstFoldingPropertyName = "constFolding"
  val ConstCachingPropertyName = "constCaching"

  def main(args: Array[String]): Unit = {

    getSetup(args) match {
      case Some(setup) =>
        val numRuns = intProperty(NumOfRunsPropertyName, DefaultNumOfRuns)
        val stepSize = intProperty(StepSizePropertyName, DefaultStepSize)
        val noCPUAccounting = booleanProperty(NoCPUAccountingPropertyName, DefaultNoCPUAccounting)
        val constFolding = optBooleanProperty(ConstFoldingPropertyName)
        val constCaching = optBooleanProperty(ConstCachingPropertyName)

        val requestedSettings = RequestedCompilerSettings(noCPUAccounting, constFolding, constCaching)
        val actualSettings = requestedSettings.toCompilerSettings

        val bm = Benchmark(dirPrefix + setup.benchmarkFile)

        println("file = \"" + bm.fileName + "\"")
        println("arguments = {")
        for (a <- setup.args) {
          println("\t\"" + a + "\"")
        }
        println("}")
        println(NumOfRunsPropertyName + " = " + numRuns)

        println(NoCPUAccountingPropertyName + " = " + requestedSettings.noCPUAccounting + " (" + actualSettings.cpuAccountingMode() + ")")
        println(ConstFoldingPropertyName + " = " + requestedSettings.constFolding + " (" + actualSettings.constFolding() + ")")
        println(ConstCachingPropertyName + " = " + requestedSettings.constCaching + " (" + actualSettings.constCaching() + ")")

        if (!noCPUAccounting) {
          println(StepSizePropertyName + " = " + stepSize)
        }
        println()


        for (i <- 1 to numRuns) {
          val prefix = s"#$i\t"
          bm.go(prefix, stepSize, actualSettings, setup.args:_*)
        }


      case None =>
        println("Usage: java " + getClass.getName + " BENCHMARK-FILE [ARG[S...]]")
        println("Use the \"" + NumOfRunsPropertyName + "\" VM property to set the number of runs (default is " + DefaultNumOfRuns + ").")
        println("        \"" + StepSizePropertyName + "\" VM property to set the step size (default is " + DefaultStepSize + ").")
        println("        \"" + NoCPUAccountingPropertyName + "\" VM property (true/false) to turn off CPU accounting (default is " + DefaultNoCPUAccounting + ")")
        System.exit(1)
    }



  }


}
