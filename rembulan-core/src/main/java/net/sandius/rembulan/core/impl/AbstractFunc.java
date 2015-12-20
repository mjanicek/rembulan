package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.Func;

public abstract class AbstractFunc implements Func {

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}
