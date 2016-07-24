package net.sandius.rembulan.lbc.recompiler.gen;

import net.sandius.rembulan.compiler.analysis.types.AbstractType;
import net.sandius.rembulan.compiler.analysis.types.LuaTypes;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;

import java.util.Map;

public class CompilationContext {

	private final Map<Prototype, CompilationUnit> units;

	public CompilationContext(Map<Prototype, CompilationUnit> units) {
		this.units = Check.notNull(units);
	}

	public AbstractType typeOf(Prototype prototype) {
		return units.containsKey(prototype) ? units.get(prototype).generic().functionType() : LuaTypes.FUNCTION;
	}

	public String prototypeClassName(Prototype prototype) {
		return units.get(prototype).name();
	}

}
