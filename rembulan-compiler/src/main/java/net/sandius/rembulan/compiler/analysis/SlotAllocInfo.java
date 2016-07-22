package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.ir.AbstractVal;
import net.sandius.rembulan.compiler.ir.Var;
import net.sandius.rembulan.util.Check;

import java.util.Map;
import java.util.NoSuchElementException;

public class SlotAllocInfo {

	private final Map<AbstractVal, Integer> valSlots;
	private final Map<Var, Integer> varSlots;
	private final int numSlots;

	public SlotAllocInfo(Map<AbstractVal, Integer> valSlots, Map<Var, Integer> varSlots) {
		this.valSlots = Check.notNull(valSlots);
		this.varSlots = Check.notNull(varSlots);

		int n = 0;
		for (Integer i : varSlots.values()) {
			n = Math.max(n, i);
		}
		for (Integer i : valSlots.values()) {
			n = Math.max(n, i);
		}
		this.numSlots = n + 1;
	}

	public int slotOf(AbstractVal v) {
		Integer idx = valSlots.get(Check.notNull(v));
		if (idx != null) {
			return idx;
		}
		else {
			throw new NoSuchElementException("Undefined slot for value: " + v);
		}
	}

	public int slotOf(Var v) {
		Integer idx = varSlots.get(Check.notNull(v));
		if (idx != null) {
			return idx;
		}
		else {
			throw new NoSuchElementException("Undefined slot for variable: " + v);
		}
	}

	public int numSlots() {
		return numSlots;
	}

}
