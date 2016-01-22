package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.Prototype;

public class NClosure extends NUnconditional {

	public final int dest;
	public final int index;

	public NClosure(int dest, int index) {
		super();
		this.dest = dest;
		this.index = index;
	}

	@Override
	public String selfToString() {
		return "Closure(" + dest + "," + index + ")";
	}

}
