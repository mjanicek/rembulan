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

import net.sandius.rembulan.Variable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class VariableMethods {

	private VariableMethods() {
		// not to be instantiated
	}

	public static Type selfTpe() {
		return Type.getType(Variable.class);
	}

	public static AbstractInsnNode constructor() {
		return new MethodInsnNode(
				INVOKESPECIAL,
				selfTpe().getInternalName(),
				"<init>",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						Type.getType(Object.class)),
				false);
	}

	public static AbstractInsnNode get() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Variable.class),
				"get",
				Type.getMethodDescriptor(
						Type.getType(Object.class)),
				false);
	}

	public static AbstractInsnNode set() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Variable.class),
				"set",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						Type.getType(Object.class)),
				false);
	}

}
