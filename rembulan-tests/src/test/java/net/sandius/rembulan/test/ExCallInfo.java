package net.sandius.rembulan.test;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ObjectStack;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.Yield;

public class ExCallInfo extends CallInfo {

	public ExCallInfo(PreemptionContext context, ObjectStack objectStack, int base) {
		super(context, objectStack, base);
	}

	@Override
	public void resume() throws ControlThrowable {
		// preamble: load previously-saved state
		Object u = objectStack.get(base + 0);
		Object v = objectStack.get(base + 1);

		switch (pc) {
			case 0:
				try {
					v = 1;
					checkPreempt();
				}
				catch (ControlThrowable yld) {
					pc = 1;
					objectStack.set(base + 0, u);
					objectStack.set(base + 1, v);
					throw yld;
				}

			case 1:
//				try {
					u = Operators.add(u, v);
//				}
//				catch (ControlThrowable yld) {
//					pc = 2; reg[0] = u; reg[1] = v; throw yld;
//				}

			case 2:
				objectStack.setTop(1);
				return;

			default:
				throw new IllegalStateException();
		}
	}

}
