package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.FunctionId;
import net.sandius.rembulan.compiler.ir.AbstractVal;
import net.sandius.rembulan.compiler.ir.PhiVal;
import net.sandius.rembulan.compiler.ir.Val;
import net.sandius.rembulan.compiler.ir.Var;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class TypeInfo {

	private final Map<AbstractVal, Type> types;
	private final Map<Var, Boolean> vars;
	private final Set<FunctionId> nestedRefs;

	protected TypeInfo(Map<AbstractVal, Type> types, Map<Var, Boolean> vars, Set<FunctionId> nestedRefs) {
		this.types = Check.notNull(types);
		this.vars = Check.notNull(vars);
		this.nestedRefs = Check.notNull(nestedRefs);
	}

	public static TypeInfo of(Map<Val, Type> valTypes, Map<PhiVal, Type> phiValTypes, Set<Var> vars, Set<Var> reifiedVars, Set<FunctionId> nestedRefs) {
		Map<AbstractVal, Type> types = new HashMap<>();
		Map<Var, Boolean> vs = new HashMap<>();

		for (Map.Entry<Val, Type> e : valTypes.entrySet()) {
			types.put(e.getKey(), e.getValue());
		}

		for (Map.Entry<PhiVal, Type> e : phiValTypes.entrySet()) {
			types.put(e.getKey(), e.getValue());
		}

		for (Var v : vars) {
			vs.put(v, reifiedVars.contains(v));
		}
		for (Var v : reifiedVars) {
			if (!vs.containsKey(v)) {
				throw new IllegalStateException("Reified variable " + v + " not found");
			}
		}

		return new TypeInfo(types, vs, Collections.unmodifiableSet(nestedRefs));
	}

	public Iterable<AbstractVal> vals() {
		return types.keySet();
	}

	public Type typeOf(AbstractVal v) {
		Check.notNull(v);

		Type t = types.get(v);
		if (t == null) {
			throw new NoSuchElementException("No type information for " + v);
		}
		else {
			return t;
		}
	}

	public Iterable<Var> vars() {
		return vars.keySet();
	}

	public boolean isReified(Var v) {
		Check.notNull(v);

		Boolean r = vars.get(v);
		if (r != null) {
			return r.booleanValue();
		}
		else {
			throw new NoSuchElementException("Variable not found: " + v);
		}
	}

	public Set<FunctionId> nestedRefs() {
		return nestedRefs;
	}

}
