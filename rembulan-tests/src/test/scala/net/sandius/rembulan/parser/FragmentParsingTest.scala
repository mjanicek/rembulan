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

package net.sandius.rembulan.parser

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintWriter}

import net.sandius.rembulan.parser.analysis.{FunctionVarInfo, NameResolver, Variable}
import net.sandius.rembulan.parser.ast._
import net.sandius.rembulan.parser.util.FormattingPrinterVisitor
import net.sandius.rembulan.test.Util
import net.sandius.rembulan.test.fragments._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

import scala.collection.JavaConverters._
import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class FragmentParsingTest extends FunSpec with MustMatchers {

  val bundles = Seq(
    BasicFragments,
    BasicLibFragments,
    CoroutineLibFragments,
    DebugLibFragments,
    IOLibFragments,
    MathLibFragments,
    MetatableFragments,
    OperatorFragments,
    StringLibFragments,
    TableLibFragments
  )

  def tryParseChunk(code: String): Chunk = {
    val bais = new ByteArrayInputStream(code.getBytes)
    new Parser(bais).Chunk()
  }

  def resolveNames(chunk: Chunk): Chunk = NameResolver.resolveNames(chunk)

  def extractVarInfo(chunk: Chunk): Map[Object, (FunctionDefExpr.Params, FunctionVarInfo)] = {
    val m = mutable.Map.empty[Object, (FunctionDefExpr.Params, FunctionVarInfo)]

    val visitor = new Transformer() {
      override def transform(chunk: Chunk) = {
        for (vi <- Option(chunk.attributes().get(classOf[FunctionVarInfo]))) {
          m += (chunk -> (FunctionDefExpr.Params.emptyVararg(), vi))
        }
        super.transform(chunk)
      }

      override def transform(e: FunctionDefExpr) = {
        for (vi <- Option(e.attributes().get(classOf[FunctionVarInfo]))) {
          m += (e -> (e.params(), vi))
        }
        super.transform(e)
      }
    }

    visitor.transform(chunk)

    m.toMap
  }

  def prettyPrint(chunk: Chunk, resolved: Boolean): String = {
    val bais = new ByteArrayOutputStream()
    val pw = new PrintWriter(bais)
    val visitor = new FormattingPrinterVisitor(pw, resolved)
    visitor.visit(chunk.block())
    pw.flush()
    String.valueOf(bais.toByteArray map { _.toChar })
  }

  for (b <- bundles) {
    describe ("from " + b.name + " :") {
      for (f <- b.all) {
        describe (f.description) {

          it ("can be parsed") {
            Util.silenced {
              val code = f.code

              println("--BEGIN--")
              println(code)
              println("---END---")

              val chunk = tryParseChunk(code)
              println("--RESULT-BEGIN--")

              val pw = new PrintWriter(System.out)
              val visitor = new FormattingPrinterVisitor(pw)
              visitor.visit(chunk.block())
              pw.flush()

              println("---RESULT-END---")

              chunk mustNot be (null)
            }
          }

          it ("pretty-printed is parsable") {
            Util.silenced {
              val prettyPrinted = prettyPrint(tryParseChunk(f.code), false)
              try {
                val reparsed = tryParseChunk(prettyPrinted)
                reparsed mustNot be (null)
              }
              catch {
                case ex: Throwable =>
                  println(prettyPrinted)
                  throw ex
              }
            }
          }

          it ("resolves names") {
            Util.silenced {
              val parsedChunk = tryParseChunk(f.code)
              val resolvedChunk = resolveNames(parsedChunk)

              println("---BEGIN---")
              val pp = prettyPrint(resolvedChunk, true)
              println(pp)
              println("----END----")

              println()

              println("VarInfos:")
              println("---------")

              for ((o, (params, varInfo)) <- extractVarInfo(resolvedChunk)) {
                val lineInfo: Option[SourceInfo] = o match {
                  case se: SyntaxElement => Option(se.sourceInfo())
                  case _ => None
                }

                val lineSuffix = lineInfo match {
                  case Some(si) => " at " + si.toString
                  case _ => ""
                }

                val numParams = params.names().size()
                val numLocals = varInfo.locals().size()
                val numUpvals = varInfo.upvalues().size()
                val declaredVararg = params.isVararg.toString
                val actualVararg = varInfo.isVararg().toString

                def varToString(v: Variable): String = v.name.value + "_" + Integer.toHexString(v.hashCode())

                println(o + lineSuffix)
                println("--> %d params, %d locals, %d upvals, vararg:%s/%s (declared/actual)".format(
                  numParams, numLocals, numUpvals, declaredVararg, actualVararg))
                println("\tparams: " + (params.names().asScala map { _.value }).mkString("(", ", ", ")"))
                println("\tlocals: " + (varInfo.locals().asScala map { varToString }).mkString("(", ", ", ")"))
                println("\tupvals: " + (varInfo.upvalues().asScala map { uv => varToString(uv.`var`()) }).mkString("(", ", ", ")"))
                println()
              }

            }
          }

        }
      }
    }
  }

}
