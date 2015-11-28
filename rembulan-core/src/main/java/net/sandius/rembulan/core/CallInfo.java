package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class CallInfo {

	public final Function function;
	public final Registers self;
	public final Registers ret;
	public final int pc;

	public CallInfo(Function function, Registers self, Registers ret, int pc) {
		Check.notNull(function);
		Check.notNull(self);
		Check.notNull(ret);
		Check.nonNegative(pc);

		this.function = function;
		this.self = self;
		this.ret = ret;
		this.pc = pc;
	}

	@Override
	public String toString() {
		return "[" + function.toString() + ", pc=" + pc + ", self=" + self.toString() + ", ret=" + ret.toString() + "]";
	}

}
