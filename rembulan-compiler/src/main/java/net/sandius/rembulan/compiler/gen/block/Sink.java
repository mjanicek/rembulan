package net.sandius.rembulan.compiler.gen.block;

public interface Sink extends Node, SuperSink {

	Src prev();

	void setPrev(Src to);

	void prependSource(Src that);

}
