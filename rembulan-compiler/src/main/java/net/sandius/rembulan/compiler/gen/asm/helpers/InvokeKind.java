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

package net.sandius.rembulan.compiler.gen.asm.helpers;

import net.sandius.rembulan.Function;
import net.sandius.rembulan.core.impl.AbstractFunction0;
import net.sandius.rembulan.core.impl.AbstractFunction1;
import net.sandius.rembulan.core.impl.AbstractFunction2;
import net.sandius.rembulan.core.impl.AbstractFunction3;
import net.sandius.rembulan.core.impl.AbstractFunction4;
import net.sandius.rembulan.core.impl.AbstractFunction5;
import net.sandius.rembulan.core.impl.AbstractFunctionAnyArg;

public abstract class InvokeKind {

	private InvokeKind() {
		// not to be instantiated or extended
	}

	// 0 means variable number of parameters packed in an array
	// n > 0 means exactly (n - 1) parameters
	public static int encode(int numOfFixedArgs, boolean vararg) {
		return vararg ? 0 : numOfFixedArgs + 1;
	}

	public static int adjust_nativeKind(int kind) {
		return kind > 0 ? (nativeClassForKind(kind) != null ? kind : 0) : 0;
	}

	public static Class<? extends Function> nativeClassForKind(int kind) {
		switch (kind) {
			case 0:  return AbstractFunctionAnyArg.class;
			case 1:  return AbstractFunction0.class;
			case 2:  return AbstractFunction1.class;
			case 3:  return AbstractFunction2.class;
			case 4:  return AbstractFunction3.class;
			case 5:  return AbstractFunction4.class;
			case 6:  return AbstractFunction5.class;
			default: return null;
		}
	}

}
