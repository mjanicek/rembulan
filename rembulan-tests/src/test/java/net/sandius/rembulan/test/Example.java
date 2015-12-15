package net.sandius.rembulan.test;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectStack;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.Registers;
import net.sandius.rembulan.util.Ptr;

public class Example extends Function {

	public Example() {
		super();
	}

	@Override
	protected boolean run(PreemptionContext preemptionContext, LuaState state, Ptr<Object> tail, ObjectStack objectStack, int base, int ret, int pc, int numResults, int flags) throws ControlThrowable {
		// registers
		Object r_1, r_2, r_3;

		Registers self = objectStack.viewFrom(base);
		Registers retAddr = objectStack.viewFrom(ret);

		// load registers
		r_1 = self.get(0);
		r_2 = self.get(1);
		r_3 = self.get(2);

		try {
			switch (pc) {
				case 0:
					r_3 = r_1;
					r_1 = r_2;
					r_2 = r_3;

					pc = 3;
					preemptionContext.account(3);

				case 3:
					retAddr.set(0, r_1);
					retAddr.set(1, r_2);
			}
		}
		catch (ControlThrowable ct) {
			// save registers to the object stack
			self.set(0, r_1);
			self.set(1, r_2);
			self.set(2, r_3);

			ct.pushCall(this, base, ret, pc, numResults, flags);

			throw ct;
		}

		return false;
	}

}
