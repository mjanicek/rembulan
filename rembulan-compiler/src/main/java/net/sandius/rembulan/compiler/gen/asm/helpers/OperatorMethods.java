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

import net.sandius.rembulan.core.RawOperators;
import net.sandius.rembulan.core.Table;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class OperatorMethods {

	public static final String RAW_OP_MOD = "rawmod";
	public static final String RAW_OP_POW = "rawpow";
	public static final String RAW_OP_IDIV = "rawidiv";

	private OperatorMethods() {
		// not to be instantiated
	}

	public static AbstractInsnNode rawBinaryOperator(String methodName, Type returnType, Type argType) {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(RawOperators.class),
				methodName,
				Type.getMethodDescriptor(
						returnType,
						argType,
						argType),
				false);
	}

	public static AbstractInsnNode stringLen() {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(RawOperators.class),
				"stringLen",
				Type.getMethodDescriptor(
						Type.INT_TYPE,
						Type.getType(String.class)),
				false);
	}

	public static AbstractInsnNode tableRawSetIntKey() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Table.class),
				"rawset",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						Type.INT_TYPE,
						Type.getType(Object.class)),
				false);
	}

}
