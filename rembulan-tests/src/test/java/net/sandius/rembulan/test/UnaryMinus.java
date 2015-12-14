package net.sandius.rembulan.test;

import net.sandius.rembulan.core.Closure;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectStack;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.Registers;

/*
function (x)
    return -x
end

function <t2.lua:5,7> (3 instructions at 0x7f9cbac04640)
1 param, 2 slots, 0 upvalues, 1 local, 0 constants, 0 functions
	1	[6]	UNM      	1 0
	2	[6]	RETURN   	1 2
	3	[7]	RETURN   	0 1
constants (0) for 0x7f9cbac04640:
locals (1) for 0x7f9cbac04640:
	0	x	1	4
upvalues (0) for 0x7f9cbac04640:
*/

public class UnaryMinus extends Closure {

	@Override
	protected Object run(PreemptionContext preemptionContext, LuaState state, ObjectStack objectStack, int base, int ret, int pc, int numResults, int flags) throws ControlThrowable {
		// registers
		Object r_0, r_1;

		Registers self = objectStack.viewFrom(base);
		Registers retAddr = objectStack.viewFrom(ret);

		// load registers
		r_0 = self.get(0);
		r_1 = self.get(1);

		try {
			switch (pc) {
				case 0:
					// UNM 1 0
					pc = 1;
					preemptionContext.account(2);  // accounting the entire block already

					r_1 = Operators.unm(r_0);

				case 1:
					// RETURN 1 2
					retAddr.set(0, r_1);

				case 2:
					// RETURN 0 1
					// dead code -- eliminated
			}
		}
		catch (ControlThrowable ct) {
			// save registers to the object stack
			self.set(0, r_0);
			self.set(1, r_1);

			ct.pushCall(this, base, ret, pc, numResults, flags);

			throw ct;
		}

		return null;
	}

}
