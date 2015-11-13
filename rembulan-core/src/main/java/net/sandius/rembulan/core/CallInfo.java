package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

// TODO: make this immutable
public class CallInfo {

	public final PreemptionContext context;
	public final Function function;
	public final ObjectStack objectStack;
	public final int base;
	public final int returnAddr;
	public final int pc;

	public CallInfo(PreemptionContext context, Function function, ObjectStack objectStack, int base, int returnAddr, int pc) {
		Check.notNull(context);
		Check.notNull(function);
		Check.notNull(objectStack);
		Check.nonNegative(base);
		Check.nonNegative(returnAddr);
		Check.nonNegative(pc);

		this.context = context;
		this.function = function;
		this.objectStack = objectStack;
		this.base = base;
		this.returnAddr = returnAddr;
		this.pc = pc;
	}

}
