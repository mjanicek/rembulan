package net.sandius.rembulan.core;

public abstract class Function {

	public void call(PreemptionContext pctx, Registers self, Registers ret, int numResults) throws ControlThrowable {
		Function tc;

		tc = run(pctx, self, ret, 0, numResults, 0);

		while (tc != null) {
			tc = tc.run(pctx, self, ret, 0, 0, CallInfo.TAILCALL);
		}
	}

	// returns non-null if the continuation is a tail call.
	// numResults:
	//   0 .. variable # of results
	//   n > 0 .. exactly (n-1) results
	protected abstract Function run(PreemptionContext pctx, Registers self, Registers ret, int pc, int numResults, int flags) throws ControlThrowable;

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}
