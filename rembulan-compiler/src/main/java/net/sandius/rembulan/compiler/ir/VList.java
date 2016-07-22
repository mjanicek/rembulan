package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class VList {

	private final List<Val> addrs;
	private final boolean multi;

	public VList(List<Val> addrs, boolean multi) {
		this.addrs = Check.notNull(addrs);
		this.multi = multi;
	}

	public List<Val> addrs() {
		return addrs;
	}

	public boolean isMulti() {
		return multi;
	}

}
