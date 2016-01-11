package net.sandius.rembulan.test

import java.io.PrintWriter
import net.sandius.rembulan.compiler.gen.ControlFlowTraversal
import net.sandius.rembulan.lbc.{PrototypePrinterVisitor, PrototypePrinter, LuaCPrototypeLoader}

object AnalysisRunner {

  def main(args: Array[String]): Unit = {

    val luacPath = "luac53"
    require (luacPath != null)

    val ploader = new LuaCPrototypeLoader(luacPath)

    println(ploader.getVersion)
    println("------------")

//    val program =
//      """
//        |if x >= 0 and x <= 10 then print(x) end
//      """.stripMargin

    val program =
    """function f(x)
      |  print(x)
      |  if x > 0 then
      |    return f(x - 1)
      |  else
      |    if x < 0 then
      |      return f(x + 1)
      |    else
      |      return 0
      |    end
      |  end
      |end
      |
      |local hi = "hello."
      |
      |function g()
      |  print(hi)
      |  for i = 'x', 0 do print(i) end
      |end
      |
      |return f(3),f(-2)
      """.stripMargin

//    val program =
//      """local f = function (x, y)
//        |    return x + y
//        |end
//        |return -1 + f(1, 3) + 39
//      """.stripMargin

//    val program =
//      """local f = function (x, y, z)
//        |    return x + y + z
//        |end
//        |return -1 + f(1, 1, 2) + 39
//      """.stripMargin

    println(program)

    val proto = ploader.load(program)

    proto.accept(new PrototypePrinterVisitor(new PrintWriter(System.out)))
//    PrototypePrinter.print(proto, new PrintWriter(System.out))

    println()
    println("Control flow")
    println("------------")
    println()

    println("Main (" + PrototypePrinter.pseudoAddr(proto) + "):")
    new ControlFlowTraversal(proto).print(System.out)

    val it = proto.getNestedPrototypes.iterator()
    while (it.hasNext) {
      val child = it.next()
      println()
      println("Child (" + PrototypePrinter.pseudoAddr(child) + "):")
      new ControlFlowTraversal(child).print(System.out)
    }

  }

}
