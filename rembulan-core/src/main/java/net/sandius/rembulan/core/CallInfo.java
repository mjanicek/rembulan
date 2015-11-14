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

	@Override
	public String toString() {
		return "[" + function.toString() + ", pc=" + pc + ", base=" + base + ", ret=" + returnAddr + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CallInfo callInfo = (CallInfo) o;

		if (base != callInfo.base) return false;
		if (returnAddr != callInfo.returnAddr) return false;
		if (pc != callInfo.pc) return false;
		return function.equals(callInfo.function);
	}

	@Override
	public int hashCode() {
		int result = function.hashCode();
		result = 31 * result + base;
		result = 31 * result + returnAddr;
		result = 31 * result + pc;
		return result;
	}

}
