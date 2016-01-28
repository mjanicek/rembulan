package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;

public class CloseUpvalues extends Linear implements SlotEffect, LocalVariableEffect {

	public final int fromIndex;

	public CloseUpvalues(int fromIndex) {
		Check.nonNegative(fromIndex);
		this.fromIndex = fromIndex;
	}

	@Override
	public String toString() {
		return "CloseUpvalues(" + fromIndex + ")";
	}

	@Override
	public Slots effect(Slots in, Prototype prototype) {
		Slots s = in;
		for (int i = fromIndex; i < in.size(); i++) {
			s = s.freshen(i);
		}

		return s;
	}

}
