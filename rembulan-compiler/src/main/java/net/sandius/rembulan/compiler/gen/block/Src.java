package net.sandius.rembulan.compiler.gen.block;

public interface Src extends Node {

	Sink next();

	void setNext(Sink to);

	void appendSink(Sink that);

}
