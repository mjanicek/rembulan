package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.lbc.Prototype;

public interface SlotEffect {

	Slots effect(Slots in, Prototype prototype);

}
