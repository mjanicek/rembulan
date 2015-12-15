package net.sandius.rembulan.test;

import net.sandius.rembulan.core.Closure;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectStack;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.Registers;
import net.sandius.rembulan.util.Ptr;

/*
function ()
    return unaryMinus(plusOne(0))
end

function <t2.lua:9,11> (7 instructions at 0x7f9cbac04810)
0 params, 3 slots, 2 upvalues, 0 locals, 1 constant, 0 functions
	1	[10]	GETUPVAL 	0 0	; unaryMinus
	2	[10]	GETUPVAL 	1 1	; plusOne
	3	[10]	LOADK    	2 -1	; 0
	4	[10]	CALL     	1 2 0
	5	[10]	TAILCALL 	0 0 0
	6	[10]	RETURN   	0 0
	7	[11]	RETURN   	0 1
constants (1) for 0x7f9cbac04810:
	1	0
locals (0) for 0x7f9cbac04810:
upvalues (2) for 0x7f9cbac04810:
	0	unaryMinus	1	1
	1	plusOne	1	0
*/
public class MinusPlus extends Closure {

	public static final Long k_1 = Long.valueOf(0);

	@Override
	protected boolean run(PreemptionContext preemptionContext, LuaState state, Ptr<Object> tail, ObjectStack objectStack, int base, int ret, int pc, int numResults, int flags) throws ControlThrowable {
		// registers
		Object r_0, r_1, r_2;

		Registers self = objectStack.viewFrom(base);
		Registers retAddr = objectStack.viewFrom(ret);

		// load registers
		r_0 = self.get(0);
		r_1 = self.get(1);
		r_2 = self.get(2);

		try {
			switch (pc) {
				case 0:  // GETUPVAL 0 0
					pc = 1;
					preemptionContext.account(2);  // accounting the entire block already

					r_0 = getUpValue(0);

				case 1:  // GETUPVAL 1 1
					r_1 = getUpValue(1);

				case 2:  // LOADK 2 -1
					r_2 = k_1;

				case 3:  // CALL 1 2 0
					pc = 4;  // store next pc

					// store registers used in the call
					self.set(1, r_1);  // call target
					self.set(2, r_2);  // call arg #1

					Operators.call(preemptionContext, state, tail, objectStack, base + 1, base + 1, 0, 0);

				case 4:  // TAILCALL 0 0 0
					// TODO: is this correct?
					tail.set(r_0);
					return true;

				case 5:  // RETURN 0 0
					// dead code -- eliminated

				case 6:  // RETURN 0 1
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

		return false;
	}

}
