package net.sandius.rembulan.test.benchmarksgame

import net.sandius.rembulan.test.BenchmarkRunner

object BinaryTreesBenchmarkRunner extends BenchmarkRunner {

  def main(args: Array[String]): Unit = {
    val n = intProperty("n", 16)
    val numRuns = intProperty("numRuns", 3)

    println("n = " + n)
    println("numRuns = " + numRuns)

    for (i <- 1 to numRuns) {
      val prefix = s"#$i\t"
      BinaryTrees.go(prefix, n.toString)
    }

  }

}
