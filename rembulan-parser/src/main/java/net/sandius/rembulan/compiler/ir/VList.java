package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class VList {

	private final List<Temp> addrs;
	private final boolean multi;

	public VList(List<Temp> addrs, boolean multi) {
		this.addrs = Check.notNull(addrs);
		this.multi = multi;
	}

	public List<Temp> addrs() {
		return addrs;
	}

	public boolean isMulti() {
		return multi;
	}

}
