package net.sandius.rembulan.test;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.Preempted;
import net.sandius.rembulan.core.PreemptionContext;

public class Example extends Function {

	@Override
	public void resume(CallInfo ci) throws ControlThrowable {
		// preamble: load previously-saved state
		Object u = ci.objectStack.get(ci.base + 0);
		Object v = ci.objectStack.get(ci.base + 1);

		switch (ci.pc) {
			case 0:
				try {
					v = 1;
					throw Preempted.INSTANCE;
//					checkPreempt();
				}
				catch (ControlThrowable yld) {
					ci.pc = 1;
					ci.objectStack.set(ci.base + 0, u);
					ci.objectStack.set(ci.base + 1, v);

					CallInfo pci = new CallInfo(ci.context, this, ci.objectStack, ci.base, 1);

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
				ci.objectStack.setTop(1);
				return;

			default:
				throw new IllegalStateException();
		}
	}

//	public Object[] inw(Object... args) {
//		return invoke(args);
//	}

//	@Override
//	public Object[] invoke(Object[] args) {
//		CallInfo ci = new ExCallInfo(null, 5);
//		ci.push(args);

//		ci.resume();

//		return new Object[] {Operators.add(args[0], args[1])};
//	}

//	public CallInfo newCallInfo(PreemptionContext context, Object[] args) {
//		throw new UnsupportedOperationException();
//		CallInfo ci = new ExCallInfo(context, 5);
//		ci.push(args);
//		return ci;
//	}

}
