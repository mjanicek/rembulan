package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;

public class NCloseUpvalues extends NUnconditional {

	public final int fromIndex;

	public NCloseUpvalues(int fromIndex) {
		Check.nonNegative(fromIndex);
		this.fromIndex = fromIndex;
	}

	@Override
	public Slots registerEffect(Prototype proto, Slots slots) {
		for (int i = fromIndex; i < slots.size(); i++) {
			slots = slots.freshen(i);
		}
		return slots;
	}

	@Override
	public String selfToString() {
		return "CloseUpvalues(" + fromIndex + ")";
	}

}
