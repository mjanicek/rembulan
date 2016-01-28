package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.lbc.Prototype;

public class Capture extends Linear implements SlotEffect, LocalVariableEffect {

	public final int index;

	public Capture(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "Capture(" + index + ")";
	}

	@Override
	public Slots effect(Slots in, Prototype prototype) {
		return in.capture(index);
	}

}
