package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.types.FunctionType;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;

import java.util.Map;

public class CompilationContext {

	private final Map<Prototype, CompilationUnit> units;

	public CompilationContext(Map<Prototype, CompilationUnit> units) {
		this.units = Check.notNull(units);
	}

	public FunctionType typeOf(Prototype prototype) {
		return units.containsKey(prototype) ? units.get(prototype).generic().functionType() : LuaTypes.FUNCTION;
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

	public Type constType(Prototype prototype, int index) {
		return constantType(prototype.getConstants().get(index));
	}

}
