package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;

public class Capture extends Linear implements SlotEffect, LocalVariableEffect {

	public final IntVector indices;

	public Capture(IntVector indices) {
		Check.notNull(indices);
		this.indices = indices;
	}

	@Override
	public String toString() {
		return "Capture(" + indices.toString(",") + ")";
	}

	@Override
	public Slots effect(Slots in) {
		Slots s = in;
		for (int i = 0; i < indices.length(); i++) {
			s = s.capture(indices.get(i));
		}
		return s;
	}

}
