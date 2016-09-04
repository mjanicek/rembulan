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

import net.sandius.rembulan.runtime.Dispatch;
import net.sandius.rembulan.runtime.ExecutionContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class DispatchMethods {

	private DispatchMethods() {
		// not to be instantiated
	}

	public static final String OP_ADD = "add";
	public static final String OP_SUB = "sub";
	public static final String OP_MUL = "mul";
	public static final String OP_DIV = "div";
	public static final String OP_MOD = "mod";
	public static final String OP_POW = "pow";
	public static final String OP_UNM = "unm";
	public static final String OP_IDIV = "idiv";

	public static final String OP_BAND = "band";
	public static final String OP_BOR = "bor";
	public static final String OP_BXOR = "bxor";
	public static final String OP_BNOT = "bnot";
	public static final String OP_SHL = "shl";
	public static final String OP_SHR = "shr";

	public static final String OP_CONCAT = "concat";
	public static final String OP_LEN = "len";

	public static final String OP_EQ = "eq";
	public static final String OP_NEQ = "neq";
	public static final String OP_LT = "lt";
	public static final String OP_LE = "le";

	public static final String OP_INDEX = "index";
	public static final String OP_SETINDEX = "setindex";

	public static final String OP_CALL = "call";

	public static AbstractInsnNode dynamic(String methodName, int numArgs) {
		ArrayList<Type> args = new ArrayList<>();
		args.add(Type.getType(ExecutionContext.class));
		for (int i = 0; i < numArgs; i++) {
			args.add(Type.getType(Object.class));
		}
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				methodName,
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						args.toArray(new Type[0])),
				false);
	}

	public static AbstractInsnNode numeric(String methodName, int numArgs) {
		Type[] args = new Type[numArgs];
		Arrays.fill(args, Type.getType(Number.class));
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				methodName,
				Type.getMethodDescriptor(
						Type.getType(Number.class),
						args),
				false);
	}

	public static AbstractInsnNode index() {
		return dynamic(OP_INDEX, 2);
	}

	public static AbstractInsnNode setindex() {
		return dynamic(OP_SETINDEX, 3);
	}

	public static int adjustKind_call(int kind) {
		return kind > 0 ? (call_method(kind).exists() ? kind : 0) : 0;
	}

	public final static int MAX_CALL_KIND;
	static {
		int k = 1;
		while (call_method(k).exists()) k += 1;
		MAX_CALL_KIND = k - 1;
	}

	private static ReflectionUtils.Method call_method(int kind) {
		return ReflectionUtils.staticArgListMethodFromKind(
				Dispatch.class, OP_CALL, new Class[] { ExecutionContext.class, Object.class }, kind);
	}

	public static AbstractInsnNode call(int kind) {
		return call_method(kind).toMethodInsnNode();
	}

	public static AbstractInsnNode continueLoop() {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				"signed_le",
				Type.getMethodDescriptor(
						Type.BOOLEAN_TYPE,
						Type.getType(Number.class),
						Type.getType(Number.class),
						Type.getType(Number.class)),
				false);
	}

}
