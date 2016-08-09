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

package net.sandius.rembulan.compiler.tf;

import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.analysis.LivenessInfo;
import net.sandius.rembulan.compiler.analysis.TypeInfo;

public class DeadCodePruner {

	public static IRFunc pruneDeadCode(IRFunc fn, TypeInfo types, LivenessInfo liveness) {
		DeadCodePrunerVisitor visitor = new DeadCodePrunerVisitor(types, liveness);
		visitor.visit(fn);
		return fn.update(visitor.result());
	}

}
