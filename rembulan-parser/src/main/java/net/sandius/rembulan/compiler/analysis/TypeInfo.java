package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.ir.AbstractVal;
import net.sandius.rembulan.compiler.ir.PhiVal;
import net.sandius.rembulan.compiler.ir.Val;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.util.Check;

import java.util.HashMap;
import java.util.Map;

public class TypeInfo {

	private final Map<AbstractVal, Type> types;

	protected TypeInfo(Map<AbstractVal, Type> types) {
		this.types = Check.notNull(types);
	}

	public static TypeInfo of(Map<Val, Type> valTypes, Map<PhiVal, Type> phiValTypes) {
		Map<AbstractVal, Type> types = new HashMap<>();

		for (Map.Entry<Val, Type> e : valTypes.entrySet()) {
			types.put(e.getKey(), e.getValue());
		}

		for (Map.Entry<PhiVal, Type> e : phiValTypes.entrySet()) {
			types.put(e.getKey(), e.getValue());
		}

		return new TypeInfo(types);
	}

	public Iterable<AbstractVal> vals() {
		return types.keySet();
	}

	public Type typeOf(AbstractVal v) {
		Check.notNull(v);

		Type t = types.get(v);
		if (t == null) {
			throw new IllegalStateException("No type information for " + v);
		}
		else {
			return t;
		}
	}

}
