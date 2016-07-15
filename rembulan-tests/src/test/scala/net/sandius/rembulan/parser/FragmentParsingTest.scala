package net.sandius.rembulan.parser

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintWriter}

import net.sandius.rembulan.parser.analysis.{FunctionVarInfo, NameResolver}
import net.sandius.rembulan.parser.ast._
import net.sandius.rembulan.parser.util.FormattingPrinterVisitor
import net.sandius.rembulan.test._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class FragmentParsingTest extends FunSpec with MustMatchers {

  val bundles = Seq(
    BasicFragments,
    BasicLibFragments,
    CoroutineFragments,
    DebugLibFragments,
    IOLibFragments,
    MathFragments,
    MetatableFragments,
    OperatorFragments,
    StringFragments,
    TableLibFragments
  )

  def exprToString(expr: Expr): String = {
    val baos = new ByteArrayOutputStream()
    val pw = new PrintWriter(baos)
    val visitor = new FormattingPrinterVisitor(pw)
    expr.accept(visitor)
    pw.flush()
    String.valueOf(baos.toByteArray map { _.toChar })
  }

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

  def prettyPrint(chunk: Chunk): String = {
    val bais = new ByteArrayOutputStream()
    val pw = new PrintWriter(bais)
    val visitor = new FormattingPrinterVisitor(pw)
    visitor.visit(chunk.block())
    pw.flush()
    String.valueOf(bais.toByteArray map { _.toChar })
  }

  def tryParseExpr(s: String): Unit = {
    println("\"" + s + "\"")

    val bais = new ByteArrayInputStream(s.getBytes)
    val parser = new Parser(bais)
    val result = parser.Expr()
    parser.Eof()

    println("--> " + exprToString(result))
    println()
  }

  describe ("expr") {

    def ok(s: String): Unit = {
      it ("ok: " + s) {
        tryParseExpr(s)
      }
    }

    def nok(s: String): Unit = {
      it ("not-ok: " + s) {
        try {
          tryParseExpr(s)
        }
        catch {
          case ex: ParseException =>
            println("--> (error:) " + ex.getMessage)
        }
      }
    }

    for ((s, good) <- Expressions.get) {
      good match {
        case true => ok(s)
        case false => nok(s)
      }
    }

  }

  for (b <- bundles) {
    describe ("from " + b.name + " :") {
      for (f <- b.all) {
        describe (f.description) {

          it ("can be parsed") {
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

          it ("pretty-printed is parsable") {
            val prettyPrinted = prettyPrint(tryParseChunk(f.code))
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

          it ("resolves names") {
            val parsedChunk = tryParseChunk(f.code)
            val resolvedChunk = resolveNames(parsedChunk)

            println("---BEGIN---")
            val pp = prettyPrint(resolvedChunk)
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

              println(o + lineSuffix)
              println("--> %d params, %d locals, %d upvals, vararg:%s/%s (declared/actual)".format(
                numParams, numLocals, numUpvals, declaredVararg, actualVararg))
              println()
            }

          }

        }
      }
    }
  }

}
