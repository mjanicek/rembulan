package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;

public interface Src extends Node {

	Sink next();

	void setNext(Sink to);

	void appendSink(Sink that);

	Src appendLinear(Linear that);

	Slots outSlots();

}
