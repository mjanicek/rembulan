package net.sandius.rembulan.test.benchmarksgame

import net.sandius.rembulan.test.BenchmarkRunner

object FannkuchBenchmarkRunner extends BenchmarkRunner {

  def main(args: Array[String]): Unit = {
    val n = intProperty("n", 10)
    val numRuns = intProperty("numRuns", 3)

    println("n = " + n)
    println("numRuns = " + numRuns)

    for (i <- 1 to numRuns) {
      val prefix = s"#$i\t"
      Fannkuch.go(prefix, n.toString)
    }

  }

}
