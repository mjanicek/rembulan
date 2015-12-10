package net.sandius.rembulan.core;

public abstract class Function {

	public void call(PreemptionContext pctx, Registers self, Registers ret) throws ControlThrowable {
		CallInfo tail;

		tail = run(pctx, self, ret, 0);

		while (tail != null) {
			tail.resume(pctx);
		}
	}

	// returns non-null if the continuation is a tail call.
	protected abstract CallInfo run(PreemptionContext pctx, Registers self, Registers ret, int pc) throws ControlThrowable;

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}
