package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Ptr;

public abstract class Function {

	public void resume(
			PreemptionContext pctx,
			LuaState state,
			Ptr<Object> tail,
			ObjectStack objectStack,
			int base,
			int ret,
			int pc,
			int numResults,
			int flags) throws ControlThrowable {

		boolean tailCalling;

		tailCalling = this.run(pctx, state, tail, objectStack, base, ret, pc, numResults, flags);

		while (tailCalling) {
			// tail-calling
			Object tcTarget = tail.getAndClear();
			tailCalling = Operators.callOnce(pctx, state, tcTarget, tail, objectStack, base, ret, 0, CallInfo.TAILCALL);
		}

		assert (tail.isNull());
	}

	public void call(PreemptionContext pctx, LuaState state, Ptr<Object> tail, ObjectStack objectStack, int base, int ret, int numResults, int flags) throws ControlThrowable {
		resume(pctx, state, tail, objectStack, base, ret, 0, numResults, flags);
	}

	// Returns true if the continuation is a tail call. In that case the tail parameter holds
	// the target of the tail call.
	// numResults:
	//   0 .. variable # of results
	//   n > 0 .. exactly (n-1) results
//	protected abstract Object run(PreemptionContext pctx, Registers self, Registers ret, int pc, int numResults, int flags) throws ControlThrowable;
	protected abstract boolean run(
			PreemptionContext preemptionContext,
			LuaState state,
			Ptr<Object> tail,
			ObjectStack objectStack,
			int base,
			int ret,
			int pc,
			int numResults,
			int flags) throws ControlThrowable;

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}
