package net.sandius.rembulan.test

import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import java.nio.ByteOrder

import net.sandius.rembulan.core._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class LoaderWriterSpec extends FunSpec with MustMatchers {

  val luacPath = "luac53"
  val luacLoader = new LuaCPrototypeLoader(luacPath)

  def escape(raw: String) = PrettyPrinter.escapedString(raw)

  def compile(program: String): Prototype = {
    val visitor = new PrototypeBuilderVisitor
    luacLoader.accept(program, visitor)
    visitor.get
  }

  def load(bytes: Array[Byte]): Prototype = {
    val visitor = new PrototypeBuilderVisitor()

    val bais = new ByteArrayInputStream(bytes)
    val loader = PrototypeLoader.fromInputStream(bais)

    loader.accept(visitor)
    visitor.get
  }

  def dump(proto: Prototype, format: BinaryChunkFormat): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val writer = new PrototypeWriter(baos, format)
    proto.accept(writer)
    baos.toByteArray
  }

  val formats = List(
    new BinaryChunkFormat(ByteOrder.LITTLE_ENDIAN, 4, 4, 4, 8, 8),
    new BinaryChunkFormat(ByteOrder.BIG_ENDIAN, 4, 4, 4, 8, 8),
    new BinaryChunkFormat(ByteOrder.LITTLE_ENDIAN, 8, 8, 8, 8, 8),
    new BinaryChunkFormat(ByteOrder.BIG_ENDIAN, 8, 8, 8, 8, 8)
  )

  def prog(program: String) {

    describe("Program \"" + escape(program) + "\"") {

      lazy val prototype = compile(program)

      it("is accepted") {
        prototype must not be null
      }

      for (fmt <- formats) {
        describe ("dumped as " + fmt) {

          lazy val dumped = dump(prototype, fmt)
          lazy val reloaded = load(dumped)

          it("can be dumped") {
            dumped must not be null
            dumped must not be empty
          }

          it("can be reloaded") {
            reloaded must not be null
          }

          it("reloaded yields a prototype equal to the original") {
            reloaded mustEqual prototype
          }
        }
      }

    }

  }

  prog("print( 2 )\n")

  prog("print( 2 )\n" +
       "print( 3 )\n")

  prog("print( 'hello, world' )\n" +
       "for i = 2,4 do\n" +
       "	print( 'i', i )\n" +
       "end\n")

  prog("a=1\n" +
       "while true do\n" +
       "  if a>10 then\n" +
       "     break\n" +
       "  end\n" +
       "  a=a+1\n" +
       "  print( a )\n" +
       "end\n")

  prog("#!../lua\n" +
       "print( 2 )\n")

  prog("A = {g=10}\n" +
       "print( A )\n")

  prog("print( 1 == b and b )\n")

}
