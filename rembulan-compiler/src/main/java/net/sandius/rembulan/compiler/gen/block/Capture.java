package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntContainer;
import net.sandius.rembulan.util.IntSet;

public class Capture extends Linear implements LocalVariableEffect {

	public final IntSet indices;

	public Capture(IntContainer indices) {
		Check.notNull(indices);
		this.indices = IntSet.empty().plus(indices);
	}

	@Override
	public String toString() {
		return "Capture(" + indices.toString(",") + ")";
	}

	@Override
	protected Slots effect(Slots in) {
		Slots s = in;
		for (int i = 0; i < indices.length(); i++) {
			s = s.capture(indices.get(i));
		}
		return s;
	}

}
