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

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class ReflectionUtils {

	public static class Method {

		public final Class<?> owner;
		public final String name;
		public final boolean isStatic;
		public final Class<?> returnType;
		public final Class<?>[] args;

		public Method(Class<?> owner, String name, boolean isStatic, Class<?> returnType, Class<?>[] args) {
			this.owner = Objects.requireNonNull(owner);
			this.name = name;
			this.isStatic = isStatic;
			this.returnType = Objects.requireNonNull(returnType);
			this.args = args != null ? args : new Class[0];
		}

		public boolean exists() {
			try {
				owner.getMethod(name, args);
				return true;
			}
			catch (NoSuchMethodException ex) {
				return false;
			}
		}

		public Type getMethodType() {
			Type[] ts = new Type[args.length];
			for (int i = 0; i < args.length; i++) {
				ts[i] = Type.getType(args[i]);
			}
			return Type.getMethodType(
					Type.getType(returnType),
					ts);
		}

		public MethodInsnNode toMethodInsnNode() {
			return new MethodInsnNode(
					isStatic ? INVOKESTATIC : (owner.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL),
					Type.getInternalName(owner),
					name,
					getMethodType().getDescriptor(),
					owner.isInterface());
		}

	}

	private static Method argListMethodFromKind(boolean isStatic, Class<?> owner, String name, Class<?>[] prefix, int kind) {
		ArrayList<Class<?>> args = new ArrayList<>();
		if (prefix != null) {
			Collections.addAll(args, prefix);
		}
		if (kind > 0) {
			for (int i = 0; i < kind - 1; i++) {
				args.add(Object.class);
			}
		}
		else {
			args.add(Object[].class);
		}

		return new Method(owner, name, isStatic, Void.TYPE, args.toArray(new Class<?>[0]));
	}

	public static Method staticArgListMethodFromKind(Class<?> owner, String name, Class<?>[] prefix, int kind) {
		return argListMethodFromKind(true, owner, name, prefix, kind);
	}

	public static Method virtualArgListMethodFromKind(Class<?> owner, String name, Class<?>[] prefix, int kind) {
		return argListMethodFromKind(false, owner, name, prefix, kind);
	}

}
