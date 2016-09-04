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

package net.sandius.rembulan.examples

import net.sandius.rembulan.exec.DirectCallExecutor
import net.sandius.rembulan.impl.DefaultLuaState

object AsyncRunner {

  def main(args: Array[String]): Unit = {

    val state = new DefaultLuaState()
    val executor = DirectCallExecutor.newExecutor(state)

    System.out.println("Calling...")
    val result = executor.call(new AsyncExample, java.lang.Long.valueOf(1000))
    System.out.println(result.mkString("Returned: [", ", ", "]"))

  }

}
