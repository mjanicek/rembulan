package net.sandius.rembulan.test;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.impl.AbstractFunc2;
import net.sandius.rembulan.util.Ptr;

public class Example extends AbstractFunc2 {

	public Example() {
		super();
	}

	protected boolean run(PreemptionContext preemptionContext, LuaState state, Ptr<Object> tail, int base, int ret, int pc, int numResults, int flags) throws ControlThrowable {
		// registers
		Object r_1, r_2, r_3;

		r_1 = 0;
		r_2 = 0;
		r_3 = 0;

//		Registers self = objectStack.viewFrom(base);
//		Registers retAddr = objectStack.viewFrom(ret);

		// load registers
//		r_1 = self.get(0);
//		r_2 = self.get(1);
//		r_3 = self.get(2);

		try {
			switch (pc) {
				case 0:
					r_3 = r_1;
					r_1 = r_2;
					r_2 = r_3;

					pc = 3;
					preemptionContext.withdraw(3);

				case 3:
//					retAddr.set(0, r_1);
//					retAddr.set(1, r_2);
			}
		}
		catch (ControlThrowable ct) {
			// save registers to the object stack
//			self.set(0, r_1);
//			self.set(1, r_2);
//			self.set(2, r_3);
//
//			ct.pushCall(this, base, ret, pc, numResults, flags);

			throw ct;
		}

		return false;
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2) throws ControlThrowable {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resume(LuaState state, ObjectSink result, Object suspendedState) throws ControlThrowable {
		throw new UnsupportedOperationException();
	}

}
