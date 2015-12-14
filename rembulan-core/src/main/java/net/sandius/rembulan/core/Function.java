package net.sandius.rembulan.core;

public abstract class Function {

	public void resume(
			PreemptionContext pctx,
			LuaState state,
			ObjectStack objectStack,
			int base,
			int ret,
			int pc,
			int numResults,
			int flags) throws ControlThrowable {

		Object tc;

		tc = this.run(pctx, state, objectStack, base, ret, pc, numResults, flags);

		while (tc != null) {
			// tail-calling
			tc = Operators.callOnce(pctx, state, tc, objectStack, base, ret, 0, CallInfo.TAILCALL);
		}
	}

	public void call(PreemptionContext pctx, LuaState state, ObjectStack objectStack, int base, int ret, int numResults, int flags) throws ControlThrowable {
		resume(pctx, state, objectStack, base, ret, 0, numResults, flags);
	}

	// returns non-null if the continuation is a tail call.
	// numResults:
	//   0 .. variable # of results
	//   n > 0 .. exactly (n-1) results
//	protected abstract Object run(PreemptionContext pctx, Registers self, Registers ret, int pc, int numResults, int flags) throws ControlThrowable;
	protected abstract Object run(
			PreemptionContext preemptionContext,
			LuaState state,
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
