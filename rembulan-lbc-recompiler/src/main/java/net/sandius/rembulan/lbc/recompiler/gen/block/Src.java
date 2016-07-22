package net.sandius.rembulan.lbc.recompiler.gen.block;

public interface Src extends Node {

	Sink next();

	void setNext(Sink to);

	void appendSink(Sink that);

	Src appendLinear(Linear that);

}
