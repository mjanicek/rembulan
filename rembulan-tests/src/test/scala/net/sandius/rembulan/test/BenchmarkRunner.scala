package net.sandius.rembulan.test

import java.io.PrintStream
import java.util.Scanner

import net.sandius.rembulan.compiler.{ChunkClassLoader, Compiler, CompilerChunkLoader}
import net.sandius.rembulan.core.impl.DefaultLuaState
import net.sandius.rembulan.core.{Exec, LuaState, PreemptionContext, Table}
import net.sandius.rembulan.lib.LibUtils
import net.sandius.rembulan.lib.impl._
import net.sandius.rembulan.test.FragmentExecTestSuite.CountingPreemptionContext

import scala.util.Try

object BenchmarkRunner {

  case class CompilerSettings(
      noCPUAccounting: Boolean,
      constFolding: Option[Boolean],
      constCaching: Option[Boolean]
  )

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

  def initEnv(state: LuaState, args: Seq[String]): Table = {
    val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))
    val coroutineLib = state.newTable()
    new DefaultCoroutineLib().installInto(state, coroutineLib)
    env.rawset("coroutine", coroutineLib)
    val mathLib = state.newTable()
    new DefaultMathLib().installInto(state, mathLib)
    env.rawset("math", mathLib)
    val stringLib = state.newTable()
    new DefaultStringLib().installInto(state, stringLib)
    env.rawset("string", stringLib)
    val ioLib = state.newTable()
    new DefaultIOLib(state).installInto(state, ioLib)
    env.rawset("io", ioLib)

    // command-line arguments
    val argTable = state.newTable()
    for ((a, i) <- args.zipWithIndex) {
      argTable.rawset(i + 1, a)
    }
    env.rawset("arg", argTable)

    env
  }

  def init(pc: PreemptionContext, settings: CompilerSettings, filename: String, args: String*): Exec = {
    val resourceStream = getClass.getResourceAsStream(filename)
    require (resourceStream != null, "resource must exist, is null")
    val sourceContents = new Scanner(resourceStream, "UTF-8").useDelimiter("\\A").next()
    require (sourceContents != null, "source contents must not be null")

    val preemptionContext = pc

    val cpuMode = settings.noCPUAccounting match {
      case true => Compiler.CPUAccountingMode.NO_CPU_ACCOUNTING
      case _ => Compiler.DEFAULT_CPU_ACCOUNTING_MODE
    }

    val ldr = new CompilerChunkLoader(
      new ChunkClassLoader(),
      cpuMode,
      settings.constFolding.getOrElse(Compiler.DEFAULT_CONST_FOLDING_MODE),
      settings.constCaching.getOrElse(Compiler.DEFAULT_CONST_CACHING_MODE))

    val state = new DefaultLuaState.Builder()
        .withPreemptionContext(preemptionContext)
        .build()

    val env = initEnv(state, args)

    val func = ldr.loadTextChunk(state.newUpvalue(env), "benchmarkMain", sourceContents)

    new Exec(state, func)
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
    val pc = new CountingPreemptionContext()

    val exec = timed (prefix + "init") {
      init(pc, settings, filename, args:_*)
    }

    var steps = 0

    val before = System.nanoTime()
    var execState = exec.getExecutionState

    while (exec.isPaused) {
      pc.deposit(stepSize)
      if (pc.allowed) {
        execState = exec.resume()
      }
      steps += 1
    }
    val after = System.nanoTime()

    val totalTimeMillis = (after - before) / 1000000.0
    val totalCPUUnitsSpent = pc.totalCost
    val avgTimePerCPUUnitNanos = (after - before).toDouble / totalCPUUnitsSpent.toDouble
    val avgCPUUnitsPerSecond = (1000000000.0 * totalCPUUnitsSpent) / (after - before)

    println(prefix + "Execution took %.1f ms".format(totalTimeMillis))
    if (!settings.noCPUAccounting) {
      println(prefix + "Total CPU cost: " + pc.totalCost + " LI")
      println(prefix)
      println(prefix + "Step size: " + stepSize + " LI")
      println(prefix + "Num of steps: " + steps)
      println(prefix + "Avg time per step: %.3f ms".format(totalTimeMillis / steps))
      println(prefix)
      println(prefix + "Avg time per unit: %.2f ns".format(avgTimePerCPUUnitNanos))
      println(prefix + "Avg units per second: %.1f LI/s".format(avgCPUUnitsPerSecond))
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

        val bm = Benchmark(dirPrefix + setup.benchmarkFile)

        println("file = \"" + bm.fileName + "\"")
        println("arguments = {")
        for (a <- setup.args) {
          println("\t\"" + a + "\"")
        }
        println("}")
        println(NumOfRunsPropertyName + " = " + numRuns)
        println(NoCPUAccountingPropertyName + " = " + noCPUAccounting)
        println(ConstFoldingPropertyName + " = " + constFolding)
        println(ConstCachingPropertyName + " = " + constCaching)
        if (!noCPUAccounting) {
          println(StepSizePropertyName + " = " + stepSize)
        }
        println()

        val settings = CompilerSettings(noCPUAccounting, constFolding, constCaching)

        for (i <- 1 to numRuns) {
          val prefix = s"#$i\t"
          bm.go(prefix, stepSize, settings, setup.args:_*)
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
