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

  def padRight(s: String, width: Int): String = {
    if (s.length >= width) s else s + fillStr(" ", width - s.length)
  }

  def tabulate(lines: Seq[String], tab: String): Seq[String] = {
    val tabbedLines = for (l <- lines) yield l.split("\t").toSeq
    val numCols = (tabbedLines map { _.size }).max
    val matrix = tabbedLines map { tl => tl ++ Seq.fill[String](numCols - tl.size)("") }

    val cols = matrix.transpose
    val colWidths = for (col <- cols) yield (col map { _.length }).max

    val padded = for (row <- matrix) yield for ((cell, width) <- row zip colWidths) yield padRight(cell, width)
    for (row <- padded) yield row.mkString(tab)
  }

}
