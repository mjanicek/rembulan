package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class CallInfo {

	public final Function function;
	public final Registers self;
	public final Registers ret;
	public final int pc;
	public final int numResults;

	public final int flags;

	public static final int TAILCALL = 0x1;
	public static final int METAMETHOD = 0x2;

	public CallInfo(Function function, Registers self, Registers ret, int pc, int numResults, int flags) {
		Check.notNull(function);
		Check.notNull(self);
		Check.notNull(ret);
		Check.nonNegative(pc);
		Check.nonNegative(numResults);

		this.function = function;
		this.self = self;
		this.ret = ret;
		this.pc = pc;
		this.numResults = numResults;
		this.flags = flags;
	}

	@Override
	public String toString() {
		return "[" + function.toString() + ", pc=" + pc + ", self=" + self.toString() + ", ret=" + ret.toString() + ", numResults=" + numResults + ", flags=" + flags + "]";
	}

	public Function resume(PreemptionContext pctx) throws ControlThrowable {
		return function.run(pctx, self, ret, pc, numResults, flags);
	}

}
