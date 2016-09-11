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

import java.io.{ByteArrayOutputStream, OutputStream, PrintStream}

import scala.util.control.NonFatal

object Util {

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

  trait Printer {

    def out: OutputStream
    def err: OutputStream

    def println(x: Any): Unit

  }

  object ConsolePrinter extends Printer {
    override def out = System.out
    override def err = System.err
    override def println(x: Any) = scala.Predef.println(x)
  }

  class BufferPrinter extends Printer {

    private val baos = new ByteArrayOutputStream()
    private val printer = new PrintStream(baos)

    def out = baos
    def err = baos

    def get = baos.toString()

    def println(x: Any): Unit = {
      printer.println(x)
    }

  }

  def silenced(body: => Unit): Unit = {

    val printer = new BufferPrinter()
    val oldOut = System.out
    System.setOut(new PrintStream(printer.out))

    try {
    }
    catch {
      case NonFatal(e) =>
        oldOut.print(printer.get)
        throw e
    }
    finally {
      System.setOut(oldOut)
    }

  }

  def timed[A](printer: Printer, name: String)(body: => A): A = {
    val before = System.nanoTime()
    val result = body
    val after = System.nanoTime()
    printer.println("%s took %.1f ms".format(name, (after - before) / 1000000.0))
    result
  }

  def timed[A](name: String)(body: => A): A = {
    timed(ConsolePrinter, name)(body)
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
