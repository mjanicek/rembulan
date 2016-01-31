package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;

// TODO: find a better name
public interface SuperSink {

	Slots inSlots();

	void pushSlots(Slots s);

}
