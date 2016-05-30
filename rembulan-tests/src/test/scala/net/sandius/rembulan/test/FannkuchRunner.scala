package net.sandius.rembulan.test

import java.io.PrintStream
import java.util.Scanner

import net.sandius.rembulan.compiler.PrototypeCompilerChunkLoader
import net.sandius.rembulan.core.impl.DefaultLuaState
import net.sandius.rembulan.core.{Exec, PreemptionContext}
import net.sandius.rembulan.lib.LibUtils
import net.sandius.rembulan.lib.impl.DefaultBasicLib
import net.sandius.rembulan.parser.LuaCPrototypeReader

import scala.util.Try

object FannkuchRunner {

  def init(filename: String, args: String*): Exec = {
    val resourceStream = getClass.getResourceAsStream(filename)
    require (resourceStream != null, "resource must exist, is null")
    val sourceContents = new Scanner(resourceStream, "UTF-8").useDelimiter("\\A").next()
    require (sourceContents != null, "source contents must not be null")

    val luacName = "luac53"
    val preemptionContext = PreemptionContext.Never.INSTANCE
    val ldr = new PrototypeCompilerChunkLoader(new LuaCPrototypeReader(luacName), getClass.getClassLoader)

    val state = new DefaultLuaState.Builder()
        .withPreemptionContext(preemptionContext)
        .build()

    val env = LibUtils.init(state, new DefaultBasicLib(new PrintStream(System.out)))

    // command-line arguments
    val argTable = state.newTable(0, 0)
    for ((a, i) <- args.zipWithIndex) {
      argTable.rawset(i + 1, a)
    }
    env.rawset("arg", argTable)

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
    val exec = timed (prefix + "init") {
      init(filename, args:_*)
    }
    timed (prefix + "execution") {
      while (exec.isPaused) {
        exec.resume()
      }
    }
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
      doFile(s"RUN #$i: ", "/fannkuchredux.lua", n.toString)
    }

  }

}
