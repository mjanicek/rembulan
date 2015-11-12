package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public abstract class CallInfo {

	public final PreemptionContext context;
	public final ObjectStack objectStack;
	public final int base;

	public int pc;

	public CallInfo(PreemptionContext context, ObjectStack objectStack, int base) {
		Check.notNull(context);
		Check.notNull(objectStack);
		Check.nonNegative(base);

		this.context = context;
		this.objectStack = objectStack;
		this.base = base;

		this.pc = 0;
	}

	public abstract void resume() throws ControlThrowable;

}
