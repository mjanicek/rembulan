package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public abstract class CallInfo {

	public final PreemptionContext context;

	public final Object[] reg;
	public int top;
	public int pc;

	public CallInfo(PreemptionContext context, int max) {
		Check.notNull(context);

		this.context = context;
		this.reg = new Object[max];
		this.top = 0;
		this.pc = 0;
	}

	public void push(Object[] args) {
		Check.notNull(args);
		System.arraycopy(args, 0, reg, top, args.length);
		top += args.length;
	}

	protected void checkPreempt() {
		if (shouldPreempt()) preempt();
	}

	protected boolean shouldPreempt() {
		return context.preempt();
	}

	protected void preempt() {
		throw Yield.INSTANCE;
	}

	public abstract void resume();

}
