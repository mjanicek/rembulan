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

import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

public abstract class ASMUtils {

	private ASMUtils() {
		// not to be instantiated
	}

	public static Type arrayTypeFor(Class<?> clazz) {
		return arrayTypeFor(clazz, 1);
	}

	public static Type arrayTypeFor(Class<?> clazz, int dimensions) {
		Check.notNull(clazz);
		if (dimensions < 1) {
			throw new IllegalArgumentException("dimensions must be at least 1");
		}

		String prefix = "[";
		for (int i = 1; i < dimensions; i++) prefix = prefix + "[";

		return Type.getType(prefix + Type.getType(clazz).getDescriptor());
	}

	public static Type typeForClassName(String className) {
		Check.notNull(className);
		return Type.getType("L" + className.replace(".", "/") + ";");
	}

	public static Type[] fillTypes(Type t, int n) {
		Type[] result = new Type[n];
		Arrays.fill(result, t);
		return result;
	}

	public static FrameNode frameSame() {
		return new FrameNode(F_SAME, 0, null, 0, null);
	}

	public static FrameNode frameSame1(Class clazz) {
		return new FrameNode(F_SAME1, 0, null, 1, new Object[] { Type.getInternalName(clazz) });
	}

	public static AbstractInsnNode checkCast(Class clazz) {
		return new TypeInsnNode(CHECKCAST, Type.getInternalName(clazz));
	}

	public static AbstractInsnNode loadInt(int i) {
		switch (i) {
			case -1: return new InsnNode(ICONST_M1);
			case 0:  return new InsnNode(ICONST_0);
			case 1:  return new InsnNode(ICONST_1);
			case 2:  return new InsnNode(ICONST_2);
			case 3:  return new InsnNode(ICONST_3);
			case 4:  return new InsnNode(ICONST_4);
			case 5:  return new InsnNode(ICONST_5);
			default: {
				if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) return new IntInsnNode(BIPUSH, i);
				else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) return new IntInsnNode(SIPUSH, i);
				else return new LdcInsnNode(i);
			}
		}
	}

	public static AbstractInsnNode loadLong(long l) {
		if (l == 0L) return new InsnNode(LCONST_0);
		else if (l == 1L) return new InsnNode(LCONST_1);
		else return new LdcInsnNode(l);
	}

	public static AbstractInsnNode loadDouble(double d) {
		// We want to distinguish -0.0 from 0.0, but -0.0 == 0.0;
		// luckily, Double.equals() distinguishes these two cases.
		if (Double.valueOf(d).equals(0.0)) return new InsnNode(DCONST_0);
		else if (d == 1.0) return new InsnNode(DCONST_1);
		else return new LdcInsnNode(d);
	}

	public static MethodInsnNode ctor(Type of, Type... args) {
		return new MethodInsnNode(
				INVOKESPECIAL,
				of.getInternalName(),
				"<init>",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						args),
				false);
	}

	public static MethodInsnNode ctor(Class clazz, Class... args) {
		Type[] argTypes = new Type[args.length];
		for (int i = 0; i < args.length; i++) {
			argTypes[i] = Type.getType(args[i]);
		}
		return ctor(Type.getType(clazz), argTypes);
	}

}
