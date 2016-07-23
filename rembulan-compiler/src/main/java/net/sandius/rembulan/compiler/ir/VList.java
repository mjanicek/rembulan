package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class VList {

	private final List<Val> addrs;
	private final MultiVal suffix;  // may be null

	public VList(List<Val> addrs, MultiVal suffix) {
		this.addrs = Check.notNull(addrs);
		this.suffix = suffix;
	}

	public List<Val> addrs() {
		return addrs;
	}

	public boolean isMulti() {
		return suffix != null;
	}

	public MultiVal suffix() {
		return suffix;
	}

}
