package net.sandius.rembulan.test;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.ObjectStack;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.Preempted;
import net.sandius.rembulan.core.PreemptionContext;

public class Example extends Function {

	@Override
	public void resume(PreemptionContext context, ObjectStack objectStack, int base, int returnBase, int pc) throws ControlThrowable {
		// preamble: load previously-saved state
		Object u = objectStack.get(base + 0);
		Object v = objectStack.get(base + 1);

		switch (pc) {
			case 0:
				try {
					v = 1;
					throw Preempted.newInstance();
//					checkPreempt();
				}
				catch (ControlThrowable yld) {
					pc = 1;
					objectStack.set(base + 0, u);
					objectStack.set(base + 1, v);

					yld.push(new CallInfo(context, this, objectStack, base, returnBase, pc));

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
