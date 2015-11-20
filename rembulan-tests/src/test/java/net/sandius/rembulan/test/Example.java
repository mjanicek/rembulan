package net.sandius.rembulan.test;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.ObjectStack;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.Preempted;

public class Example extends Function {

	public Example() {
		super();
	}

	@Override
	public void resume(Coroutine coroutine, int base, int returnBase, int pc) throws ControlThrowable {
		// preamble: load previously-saved state

		Object u, v;

		coroutine.getOwnerState().shouldPreemptNow();

		{
			ObjectStack os = coroutine.getObjectStack();
			u = os.get(base + 0);
			v = os.get(base + 1);
		}

		switch (pc) {
			case 0:
				try {
					v = 1;
					throw Preempted.newInstance();
//					checkPreempt();
				}
				catch (ControlThrowable yld) {
					pc = 1;

					// save registers to the object stack
					{
						ObjectStack os = coroutine.getObjectStack();
						os.set(base + 0, u);
						os.set(base + 1, v);
					}

					yld.push(new CallInfo(this, base, returnBase, pc));

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
				{
					ObjectStack os = coroutine.getObjectStack();
					os.setTop(1);
				}
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
