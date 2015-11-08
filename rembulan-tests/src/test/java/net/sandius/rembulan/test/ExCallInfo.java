package net.sandius.rembulan.test;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.Yield;

public class ExCallInfo extends CallInfo {

	public ExCallInfo(PreemptionContext context, int max) {
		super(context, max);
	}

	@Override
	public void resume() throws ControlThrowable {
		// preamble: load previously-saved state
		Object u = reg[0];
		Object v = reg[1];

		switch (pc) {
			case 0:
				try {
					v = 1;
					checkPreempt();
				}
				catch (ControlThrowable yld) {
					pc = 1; reg[0] = u; reg[1] = v; throw yld;
				}

			case 1:
//				try {
					u = Operators.add(u, v);
//				}
//				catch (ControlThrowable yld) {
//					pc = 2; reg[0] = u; reg[1] = v; throw yld;
//				}

			case 2:
				top = 1;
				return;

			default:
				throw new IllegalStateException();
		}
	}

}
