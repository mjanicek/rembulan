package net.sandius.rembulan.test

import net.sandius.rembulan.util.IntBuffer

object Util {

  def intBufferToSeq(ib: IntBuffer): IndexedSeq[Int] = for (i <- 0 to ib.length() - 1) yield ib.get(i)

  def fillStr(pattern: String, width: Int): String = {
    val bld = new StringBuilder()
    while (bld.length < width) {
      val rem = width - bld.length
      bld.append(pattern.substring(0, math.min(rem, pattern.length)))
    }
    bld.toString()
  }

  def padRight(s: String, width: Int, pattern: String = " "): String = {
    if (s.length >= width) s else s + fillStr(pattern, width - s.length)
  }

  def tabulate(lines: Seq[String], tab: String, separator: String): Seq[String] = {
    val tabbedLines = for (l <- lines) yield l.split(separator).toSeq
    val numCols = (tabbedLines map { _.size }).max
    val matrix = tabbedLines map { tl => tl ++ Seq.fill[String](numCols - tl.size)("") }

    val cols = matrix.transpose
    val colWidths = for (col <- cols) yield (col map { _.length }).max

    val padded = for (row <- matrix) yield for ((cell, width) <- row zip colWidths) yield padRight(cell, width)
    for (row <- padded) yield row.mkString(tab)
  }

  def timed[A](name: String)(body: => A): A = {
    val before = System.nanoTime()
    val result = body
    val after = System.nanoTime()
    System.out.println("%s took %.1f ms".format(name, (after - before) / 1000000.0))
    result
  }

  def separator: String = {
    Util.fillStr("- ", 76)
  }

  def header(text: String): String = {
    Util.padRight("-- " + text + " ", 76, "-") + "\n"
  }

  def section(title: String)(body: => Unit): Unit = {
    println(header(title))
    body
    println()
  }

}
