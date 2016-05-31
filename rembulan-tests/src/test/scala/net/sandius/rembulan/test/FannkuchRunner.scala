package net.sandius.rembulan.test

import java.io.PrintStream
import java.util.Scanner

import net.sandius.rembulan.compiler.PrototypeCompilerChunkLoader
import net.sandius.rembulan.core.impl.DefaultLuaState
import net.sandius.rembulan.core.{Exec, LuaState, PreemptionContext, Table}
import net.sandius.rembulan.lib.LibUtils
import net.sandius.rembulan.lib.impl._
import net.sandius.rembulan.parser.LuaCPrototypeReader
import net.sandius.rembulan.test.FragmentExecTestSuite.CountingPreemptionContext

import scala.util.Try

object FannkuchRunner {

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
    new DefaultIOLib(state, null, System.out).installInto(state, ioLib)
    env.rawset("io", ioLib)

    // command-line arguments
    val argTable = state.newTable()
    for ((a, i) <- args.zipWithIndex) {
      argTable.rawset(i + 1, a)
    }
    env.rawset("arg", argTable)

    env
  }

  def init(pc: PreemptionContext, filename: String, args: String*): Exec = {
    val resourceStream = getClass.getResourceAsStream(filename)
    require (resourceStream != null, "resource must exist, is null")
    val sourceContents = new Scanner(resourceStream, "UTF-8").useDelimiter("\\A").next()
    require (sourceContents != null, "source contents must not be null")

    val luacName = "luac53"
    val preemptionContext = pc
    val ldr = new PrototypeCompilerChunkLoader(new LuaCPrototypeReader(luacName), getClass.getClassLoader)

    val state = new DefaultLuaState.Builder()
        .withPreemptionContext(preemptionContext)
        .build()

    val env = initEnv(state, args)

    val func = ldr.loadTextChunk(state.newUpvalue(env), "fannkuch", sourceContents)

    val exec = new Exec(state)
    exec.init(func)
    exec
  }

  def timed[A](name: String)(body: => A): A = {
    val before = System.nanoTime()
    val result = body
    val after = System.nanoTime()

    val totalTimeMillis = (after - before) / 1000000.0
    println("%s took %.1f ms".format(name, totalTimeMillis))
    result
  }

  def doFile(prefix: String, filename: String, args: String*): Unit = {
    val stepSize = 1000000
    val pc = new CountingPreemptionContext()

    val exec = timed (prefix + "init") {
      init(pc, filename, args:_*)
    }

    var steps = 0

    val before = System.nanoTime()
    while (exec.isPaused) {
      pc.deposit(stepSize)
      if (pc.allowed) {
        exec.resume()
      }
      steps += 1
    }
    val after = System.nanoTime()

    val totalTimeMillis = (after - before) / 1000000.0
    val totalCPUUnitsSpent = pc.totalCost
    val avgTimePerCPUUnitNanos = (after - before).toDouble / totalCPUUnitsSpent.toDouble
    val avgCPUUnitsPerSecond = (1000000000.0 * totalCPUUnitsSpent) / (after - before)

    println(prefix + "Execution took %.1f ms".format(totalTimeMillis))
    println(prefix + "Total CPU cost: " + pc.totalCost + " LI")
    println(prefix)
    println(prefix + "Step size: " + stepSize + " LI")
    println(prefix + "Num of steps: " + steps)
    println(prefix + "Avg time per step: %.3f ms".format(totalTimeMillis / steps))
    println(prefix)
    println(prefix + "Avg time per unit: %.2f ns".format(avgTimePerCPUUnitNanos))
    println(prefix + "Avg units per second: %.1f LI/s".format(avgCPUUnitsPerSecond))
    println()
  }

  private def intProperty(key: String, default: Int): Int = {
    Option(System.getProperty(key)) flatMap { s => Try(s.toInt).toOption } getOrElse default
  }

  def main(args: Array[String]): Unit = {
    val n = intProperty("n", 10)
    val numRuns = intProperty("numRuns", 3)

    println("n = " + n)
    println("numRuns = " + numRuns)

    for (i <- 1 to numRuns) {
      doFile(s"#$i\t", "/fannkuchredux.lua", n.toString)
    }

  }

}
