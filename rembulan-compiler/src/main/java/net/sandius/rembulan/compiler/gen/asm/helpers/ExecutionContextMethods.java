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

import net.sandius.rembulan.core.ExecutionContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;

public abstract class ExecutionContextMethods {

	private ExecutionContextMethods() {
		// not to be instantiated or extended
	}

	private static Type selfTpe() {
		return Type.getType(ExecutionContext.class);
	}

	public static InsnList registerTimeSlice(int cost) {
		InsnList il = new InsnList();

		il.add(ASMUtils.loadInt(cost));
		il.add(new MethodInsnNode(
				INVOKEINTERFACE,
				selfTpe().getInternalName(),
				"registerTicks",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						Type.INT_TYPE),
				true));

		return il;
	}

	public static MethodInsnNode checkCallYield() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				selfTpe().getInternalName(),
				"checkCallYield",
				Type.getMethodDescriptor(
						Type.VOID_TYPE),
				true);
	}

}
