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

import net.sandius.rembulan.core.Userdata
import net.sandius.rembulan.test.{FragmentBundle, FragmentExpectations, OneLiners}

object IOLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (IOContext) {

    about ("io.output") {

      program ("return io.output()") succeedsWith (classOf[Userdata])

    }

    about ("file:__tostring") {

      program ("return tostring(io.output())") succeedsWith stringStartingWith("file (0x")

    }

    about ("io.write") {

      // TODO: check what is written to the output

      program ("return io.write(1, 2)") succeedsWith (classOf[Userdata])
      program ("""return io.write({})""") failsWith "bad argument #"<<"1">>" to 'write' (string expected, got table)"

    }


  }

}
