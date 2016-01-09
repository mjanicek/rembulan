package net.sandius.rembulan.test

import java.io.PrintWriter

import net.sandius.rembulan.core._
import net.sandius.rembulan.util.Cons
import net.sandius.rembulan.{core => lua}

object Runner {

  def consToList[A](cons: Cons[A]): List[A] = {
    if (cons == null) Nil else cons.car :: consToList(cons.cdr)
  }

  def inspect(idx: Int, coro: Coroutine, callStack: Cons[CallInfo], cpuTime: Int): Unit = {
//    val os = coro.getObjectStack
//    var max = 0
//    for (i <- 0 until os.getMaxSize) {
//      if (os.get(i) != null) {
//        max = i
//      }
//    }

    println(idx + ": {")
    println("\tCPU time: " + cpuTime)
    println("\tObject stack: (not available)")
//    println("\tObject stack: " + (for (i <- 0 to max) yield i + ":[" + os.get(i) + "]").mkString(" "))
    println("\tCall stack: {")
    val cs = consToList(callStack)
    if (cs.nonEmpty) {
      println((for (ci <- cs) yield "\t\t" + ci).mkString("\n"))
    }
    println("\t\t// bottom")
    println("\t}")

    println("}")
  }

  def doRun(func: lua.Invokable, args: AnyRef*): Unit = {
    val st = DummyLuaState.newDummy(true)
    val coro = st.getCurrentCoroutine
//    val addr = coro.getObjectStack.rootView()

//    for ((v, idx) <- args.zipWithIndex) {
//      addr.set(idx, v)
//    }

    LuaState.setCurrentState(st)

    var cpuTime = 0

    val preempt = new PreemptionContext {
      override def withdraw(cost: Int) = {
        cpuTime += cost
        throw new Preempted
      }
    }

    val exec = new Exec(preempt, st)

    exec.pushCall(new CallInfo(func, 0, 0, 0, 0, 0))

    var idx = 0

    inspect(idx, coro, exec.getCallStack, cpuTime)

    while (exec.isPaused) {
      exec.resume()
      idx += 1
      inspect(idx, coro, exec.getCallStack, cpuTime)
    }

    LuaState.unsetCurrentState()
  }

  def main(args: Array[String]): Unit = {
//    val luacPath = System.getProperty("pathToLuaC")
    val luacPath = "luac53"
    require (luacPath != null)

    val ploader = new LuaCPrototypeLoader(luacPath)

    val program =
      """local f = function (x)
        |    return x + 1
        |end
        |return -1 + f(3) + 39
      """.stripMargin

//    val program =
//      """local f = 0
//        |f(1)
//      """.stripMargin

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

    System.err.println(program)

    val proto = ploader.load(program)

    PrototypePrinter.print(proto, new PrintWriter(System.err))

    val name = "lua.tmp.FortyTwo"

    val loader = new PrototypeClassLoader(name)
    val mainChunk = loader.install(proto)

    val clazz = loader.findClass(mainChunk).asInstanceOf[Class[lua.Invokable]]
    val func = clazz.getConstructor().newInstance()

    doRun(func)
  }

}
