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

package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.Table
import net.sandius.rembulan.runtime.LuaFunction
import net.sandius.rembulan.test.{FragmentBundle, FragmentExpectations, OneLiners}

object ModuleLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (ModuleContext) {

    about ("require") {

      program ("""require()""") failsWith "bad argument #1 to 'require' (string expected, got no value)"
      program ("""require(nil)""") failsWith "bad argument #1 to 'require' (string expected, got nil)"
      program ("""require(true)""") failsWith "bad argument #1 to 'require' (string expected, got boolean)"
      program ("""require({})""") failsWith "bad argument #1 to 'require' (string expected, got table)"

      program ("""require("x")""") failsWith "module 'x' not found:"<<""
      program ("""require(12.0)""") failsWith "module '12.0' not found:"<<""
      program ("""require(1/0)""") failsWith "module 'inf' not found:"<<""

      program (
        """-- caches package.loaded
          |local p = package
          |package.loaded = nil
          |local rq = require "package"
          |return rq, rq == p
        """
      ) succeedsWith (classOf[Table], true)

      program (
        """-- caches the package table
          |package = nil
          |return require "x"
        """
      ) failsWith "module 'x' not found:\n\tno field package.preload['x']"<<""

      program (
        """-- accesses package.searchers via the cached package table
          |local p = package
          |package = nil
          |p.searchers = nil
          |return require "x"
        """
      ) failsWith "'package.searchers' must be a table"

      program ("""package.loaded.hello = "world"; return require "hello"""") succeedsWith "world"

    }

    about ("package.loaded") {

      program ("return package.loaded") succeedsWith (classOf[Table])

      in (FullContext) {

        program ("return package.loaded._G, package.loaded._G == _ENV") succeedsWith (classOf[Table], true)

        val loaded = Seq("package", "coroutine", "math", "string", "table", "io", "os", "utf8", "debug")

        for (n <- loaded) {
          program (s"local p = package.loaded.$n; return p, p == $n") succeedsWith (classOf[Table], true)
        }

        // exactly all are in package.loaded
        val all = Seq("_G") ++ loaded
        program ("local n = 0; for k,v in pairs(package.loaded) do n = n + 1 end; return n") succeedsWith (all.size)

      }

    }

    about ("package.searchers") {

      program ("return package.searchers") succeedsWith (classOf[Table])

      program ("return package.searchers[1]") succeedsWith (classOf[LuaFunction])

    }


  }

}
