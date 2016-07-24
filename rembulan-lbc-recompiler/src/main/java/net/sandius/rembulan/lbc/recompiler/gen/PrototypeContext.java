package net.sandius.rembulan.lbc.recompiler.gen;

import net.sandius.rembulan.compiler.analysis.types.AbstractType;
import net.sandius.rembulan.compiler.analysis.types.LuaTypes;
import net.sandius.rembulan.compiler.analysis.types.Type;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;

public class PrototypeContext {

	private final CompilationContext compilationContext;
	private final Prototype prototype;
	private final Prototype parent;

	public PrototypeContext(CompilationContext compilationContext, Prototype prototype, Prototype parent) {
		this.compilationContext = Check.notNull(compilationContext);
		this.prototype = Check.notNull(prototype);
		this.parent = parent;
	}

	public CompilationContext compilationContext() {
		return compilationContext;
	}

	public static Type constantType(Object k) {
		if (k == null) return LuaTypes.NIL;
		else if (k instanceof Boolean) return LuaTypes.BOOLEAN;
		else if (k instanceof Double || k instanceof Float) return LuaTypes.NUMBER_FLOAT;
		else if (k instanceof Number) return LuaTypes.NUMBER_INTEGER;
		else if (k instanceof String) return LuaTypes.STRING;
		else {
			throw new IllegalStateException("Unknown constant: " + k);
		}
	}

	@Deprecated
	public Prototype prototype() {
		return prototype;
	}

	public Object getConst(int index) {
		return prototype.getConstants().get(index);
	}

	public Type constType(int index) {
		return constantType(prototype.getConstants().get(index));
	}

	public AbstractType nestedPrototypeType(int index) {
		return compilationContext().typeOf(nestedPrototype(index));
	}

	public String nestedPrototypeName(int index) {
		return compilationContext().prototypeClassName(nestedPrototype(index));
//		return PrototypePrinter.pseudoAddr(nestedPrototype(index));
	}

	@Deprecated
	public Prototype nestedPrototype(int index) {
		return prototype.getNestedPrototypes().get(index);
	}

	public String className() {
		return compilationContext().prototypeClassName(prototype);
	}

	public String parentClassName() {
		if (parent != null) {
			return compilationContext().prototypeClassName(parent);
		}
		else {
			return null;
		}
	}

	public String upvalueName(int idx) {
		return prototype.getUpValueDescriptions().get(idx).name;
	}

}
