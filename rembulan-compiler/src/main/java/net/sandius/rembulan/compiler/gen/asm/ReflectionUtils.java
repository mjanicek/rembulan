package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.Collections;

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
			this.owner = Check.notNull(owner);
			this.name = name;
			this.isStatic = isStatic;
			this.returnType = Check.notNull(returnType);
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

	private static Method argListMethodFromKind(boolean isStatic, Class<?> owner, String name, Class<?> returnType, Class<?>[] prefix, int kind) {
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

		return new Method(owner, name, isStatic, returnType, args.toArray(new Class<?>[0]));
	}

	public static Method staticArgListMethodFromKind(Class<?> owner, String name, Class<?> returnType, Class<?>[] prefix, int kind) {
		return argListMethodFromKind(true, owner, name, returnType, prefix, kind);
	}

	public static Method virtualArgListMethodFromKind(Class<?> owner, String name, Class<?> returnType, Class<?>[] prefix, int kind) {
		return argListMethodFromKind(false, owner, name, returnType, prefix, kind);
	}

}
