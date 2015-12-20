package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Ptr;

public class CallInfo {

	public final Function function;
	public final int base;
	public final int ret;
	public final int pc;
	public final int numResults;

	public final int flags;

	public static final int TAILCALL = 0x1;
	public static final int METAMETHOD = 0x2;

	public CallInfo(Function function, int base, int ret, int pc, int numResults, int flags) {
		Check.notNull(function);
		Check.nonNegative(base);
		Check.nonNegative(ret);
		Check.nonNegative(pc);
		Check.nonNegative(numResults);

		this.function = function;
		this.base = base;
		this.ret = ret;
		this.pc = pc;
		this.numResults = numResults;
		this.flags = flags;
	}

	@Override
	public String toString() {
		return "[" + function.toString() + ", pc=" + pc + ", base=" + base + ", ret=" + ret + ", numResults=" + numResults + ", flags=" + flags + "]";
	}

	@Deprecated
	public void resume() throws ControlThrowable {
		throw new UnsupportedOperationException();
	}

}
