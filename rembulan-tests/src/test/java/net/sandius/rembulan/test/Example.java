package net.sandius.rembulan.test;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.Preempted;
import net.sandius.rembulan.core.Registers;

public class Example extends Function {

	public Example() {
		super();
	}

	@Override
	protected void run(Registers own, Registers ret, int pc) throws ControlThrowable {
		// preamble: load previously-saved state

		Object u, v;

		LuaState.getCurrentState().shouldPreemptNow();

		u = own.get(0);
		v = own.get(1);

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
					own.set(0, u);
					own.set(1, v);

//					yld.push(new CallInfo(this, base, returnBase, pc));

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
//				{
//					ObjectStack os = coroutine.getObjectStack();
//					os.setTop(1);
//				}
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
