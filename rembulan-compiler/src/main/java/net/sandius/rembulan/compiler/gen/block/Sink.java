package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;

public interface Sink extends Node {

	Src prev();

	void setPrev(Src to);

	void prependSource(Src that);

	Slots inSlots();

}
