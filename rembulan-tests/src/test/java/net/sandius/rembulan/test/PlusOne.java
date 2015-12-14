package net.sandius.rembulan.test;

import net.sandius.rembulan.core.Closure;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.Registers;

/*
function (x)
    return x + 1
end

function <t2.lua:1,3> (3 instructions at 0x7f9cbac046e0)
1 param, 2 slots, 0 upvalues, 1 local, 1 constant, 0 functions
	1	[2]	ADD      	1 0 -1	; - 1
	2	[2]	RETURN   	1 2
	3	[3]	RETURN   	0 1
constants (1) for 0x7f9cbac046e0:
	1	1
locals (1) for 0x7f9cbac046e0:
	0	x	1	4
upvalues (0) for 0x7f9cbac046e0:
 */
public class PlusOne extends Closure {

	public static final Long k_1 = Long.valueOf(1);

	@Override
	protected Function run(PreemptionContext pctx, Registers self, Registers ret, int pc, int numResults, int flags) throws ControlThrowable {
		// registers
		Object r_0, r_1;

		// load registers
		r_0 = self.get(0);
		r_1 = self.get(1);

		try {
			switch (pc) {
				case 0:  // ADD 1 0 -1
					pc = 1;
					pctx.account(2);  // accounting the entire block already

					r_1 = Operators.add(r_0, k_1);

				case 1:  // RETURN 1 2
					ret.set(0, r_1);

				case 2:  // RETURN 0 1
					// dead code -- eliminated
			}
		}
		catch (ControlThrowable ct) {
			// save registers to the object stack
			self.set(0, r_0);
			self.set(1, r_1);

			ct.pushCall(this, self, ret, pc, numResults, flags);

			throw ct;
		}

		return null;
	}

}
