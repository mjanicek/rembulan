package net.sandius.rembulan.test

import net.sandius.rembulan.compiler.ChunkClassLoader
import net.sandius.rembulan.core._
import net.sandius.rembulan.core.impl.DefaultLuaState
import net.sandius.rembulan.lbc.recompiler.gen.ChunkCompiler
import net.sandius.rembulan.{core => lua}

object Runner {

  import Util._

  val loader = new LuaCFragmentCompiler("luac53")

  def main(args: Array[String]): Unit = {

    val program: Fragment = args.toList.headOption match {
      case Some(n) => BasicFragments.lookup(n).get
      case _ => throw new RuntimeException("No program name (1st command-line argument) specified!")
    }

    val proto = loader.compile(program)

    section("LuaC version") {
      println(loader.version)
    }

    section("Program code") {
      println(program.code)
    }

    val compiler = new ChunkCompiler()
    val classLoader = new ChunkClassLoader()

    section ("Compilation") {

      val chunk = timed("Compile") {
        compiler.compile(proto, "test")
      }

      val name = classLoader.install(chunk)
      val clazz = classLoader.loadClass(name).asInstanceOf[Class[lua.Function]]

      val state = new DefaultLuaState.Builder().build()
      val os = state.newObjectSink()
      val env = state.newTable()
      val upEnv = state.newUpvalue(env)

      val f = clazz.getConstructor(classOf[Upvalue]).newInstance(upEnv)

      val context = new ExecutionContext {
        override def getState = state
        override def getObjectSink = os
        override def getCurrentCoroutine = ???
        override def newCoroutine(function: Function) = ???
        override def canYield = false
      }

      section("Call") {
        timed("Call") {
          Dispatch.call(context, f)
        }
      }

      section("Result") {
        val result = os.toArray.toSeq
        for ((v, i) <- result.zipWithIndex) {
          println(i + ":" + "\t" + v + " (" + (if (v != null) v.getClass.getName else "null") + ")")
        }
      }

    }
  }

}
