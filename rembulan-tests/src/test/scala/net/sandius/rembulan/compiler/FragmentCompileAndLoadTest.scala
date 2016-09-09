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

package net.sandius.rembulan.compiler

import net.sandius.rembulan.Variable
import net.sandius.rembulan.load.ChunkClassLoader
import net.sandius.rembulan.runtime.LuaFunction
import net.sandius.rembulan.test.fragments.BasicFragments
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

@RunWith(classOf[JUnitRunner])
class FragmentCompileAndLoadTest extends FunSpec with MustMatchers {

  def withProperty[A](key: String, value: String)(body: => A): A = {
    val oldValue = System.getProperty(key)
    try {
      System.setProperty(key, value)
      body
    }
    finally {
      if (oldValue == null) {
        System.clearProperty(key)
      }
      else {
        System.setProperty(key, oldValue)
      }
    }
  }

  describe ("fragment") {

    val bundle = BasicFragments

    for (fragment <- bundle.all) {

      describe (fragment.description) {

        val settings = CompilerSettings.defaultSettings()

        it ("can be compiled to Java bytecode") {
          println("-- CODE BEGIN --")
          println(fragment.code)
          println("--- CODE END ---")
          println()

          withProperty("net.sandius.rembulan.compiler.VerifyAndPrint", "true") {
            val compiler = new LuaCompiler(settings)
            val cm = compiler.compile(fragment.code, "stdin", "test")
            cm must not be null
          }
        }

        it ("can be loaded by the VM") {
          val classLoader = new ChunkClassLoader()

          val cm = withProperty("net.sandius.rembulan.compiler.VerifyAndPrint", "true") {
            val compiler = new LuaCompiler(settings)
            compiler.compile(fragment.code, "stdin", "test")
          }

          val name = classLoader.install(cm)
          val clazz = classLoader.loadClass(name).asInstanceOf[Class[LuaFunction]]

          val f = try {
            clazz.getConstructor(classOf[Variable]).newInstance(new Variable(null))
          }
          catch {
            case ex: VerifyError => throw new IllegalStateException(ex)
          }

          f must not be null
        }

      }
    }
  }

}
