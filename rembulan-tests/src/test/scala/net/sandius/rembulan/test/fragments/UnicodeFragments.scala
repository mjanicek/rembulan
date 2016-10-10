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

import java.nio.charset.StandardCharsets

import net.sandius.rembulan.test.{FragmentBundle, FragmentExpectations, OneLiners}

object UnicodeFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (FullContext) {

    // in UTF-8
    val s = "Nechť již hříšné saxofony ďáblů rozzvučí síň úděsnými tóny waltzu, tanga a quickstepu."

    // in Lua syntax
    val lua = "Nech\\u{0165} ji\\u{017E} h\\u{0159}\\u{00ED}\\u{0161}n\\u{00E9} saxofony \\u{010F}\\u{00E1}bl\\u{016F} rozzvu\\u{010D}\\u{00ED} s\\u{00ED}\\u{0148} \\u{00FA}d\\u{011B}sn\\u{00FD}mi t\\u{00F3}ny waltzu, tanga a quickstepu."

    def utf8Length(s: String) = s.getBytes(StandardCharsets.UTF_8).length

    program (s"""return '$s'""") succeedsWith (s)
    program (s"""return #('$s')""") succeedsWith (utf8Length(s))
    program (s"""return #('$lua')""") succeedsWith (103)

    program (s"""return '$lua'""") succeedsWith (s)
    program (s"""return #('$lua')""") succeedsWith (utf8Length(s))
    program (s"""return #('$lua')""") succeedsWith (103)

  }

}
