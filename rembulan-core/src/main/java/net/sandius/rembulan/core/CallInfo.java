package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class CallInfo {

	public final Function function;
	public final int base;
	public final int returnAddr;
	public final int pc;

	public CallInfo(Function function, int base, int returnAddr, int pc) {
		Check.notNull(function);
		Check.nonNegative(base);
		Check.nonNegative(returnAddr);
		Check.nonNegative(pc);

		this.function = function;
		this.base = base;
		this.returnAddr = returnAddr;
		this.pc = pc;
	}

	// TODO: equals and hashCode

}
