package net.sandius.rembulan.lbc.recompiler.gen.block;

public interface Sink extends Node {

	Src prev();

	void setPrev(Src to);

	void prependSource(Src that);

}
