package net.sandius.rembulan.test;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.Registers;

public class Example extends Function {

	public Example() {
		super();
	}

	@Override
	protected CallInfo run(PreemptionContext pctx, Registers self, Registers ret, int pc) throws ControlThrowable {
		// registers
		Object r_1, r_2, r_3;

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
					pctx.account(3);

				case 3:
					ret.set(0, r_1);
					ret.set(1, r_2);
			}
		}
		catch (ControlThrowable ct) {
			// save registers to the object stack
			self.set(0, r_1);
			self.set(1, r_2);
			self.set(2, r_3);

			ct.push(new CallInfo(this, self, ret, pc));

			throw ct;
		}

		return null;
	}

}
