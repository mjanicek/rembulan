package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.impl.Function0;
import net.sandius.rembulan.core.impl.Function1;
import net.sandius.rembulan.core.impl.Function2;
import net.sandius.rembulan.core.impl.Function3;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.asm.ASMUtils;
import org.objectweb.asm.Type;

import java.util.ArrayList;

public abstract class InvokeKind {

	private InvokeKind() {
		// not to be instantiated or extended
	}

	// 0 means variable number of parameters packed in an array
	// n > 0 means exactly (n - 1) parameters
	public static int encode(int numOfFixedArgs, boolean vararg) {
		Check.nonNegative(numOfFixedArgs);
		return (!vararg && numOfFixedArgs >= 0 && numOfFixedArgs < 4) ? numOfFixedArgs + 1 : 0;
	}

	public static Class<? extends Function> nativeClassForKind(int kind) {
		switch (kind) {
			case 0: return FunctionAnyarg.class;
			case 1: return Function0.class;
			case 2: return Function1.class;
			case 3: return Function2.class;
			case 4: return Function3.class;
			default: throw new IllegalArgumentException("No native class for kind: " + kind);
		}
	}

	private static Type methodTypeForKind(boolean isStatic, int kind) {
		Check.nonNegative(kind);

		ArrayList<Type> args = new ArrayList<>();
		args.add(Type.getType(LuaState.class));
		args.add(Type.getType(ObjectSink.class));
		if (isStatic) {
			args.add(Type.getType(Object.class));
		}

		if (kind > 0) {
			// (kind - 1) arguments
			Type o = Type.getType(Object.class);
			for (int i = 0; i < kind - 1; i++) {
				args.add(o);
			}
		}
		else {
			// variable number of arguments, packed in an array
			args.add(ASMUtils.arrayTypeFor(Object.class));
		}

		return Type.getMethodType(
				Type.VOID_TYPE,
				args.toArray(new Type[0]));
	}

	public static Type virtualMethodType(int kind) {
		return methodTypeForKind(false, kind);
	}

	public static Type staticMethodType(int kind) {
		return methodTypeForKind(true, kind);
	}

}
