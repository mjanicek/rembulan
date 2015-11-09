package net.sandius.rembulan.test;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.PreemptionContext;

public class Example extends Function {

	public Object[] inw(Object... args) {
		return invoke(args);
	}

	@Override
	public Object[] invoke(Object[] args) {
//		CallInfo ci = new ExCallInfo(null, 5);
//		ci.push(args);

//		ci.resume();

		return new Object[] {Operators.add(args[0], args[1])};
	}

	public CallInfo newCallInfo(PreemptionContext context, Object[] args) {
		throw new UnsupportedOperationException();
//		CallInfo ci = new ExCallInfo(context, 5);
//		ci.push(args);
//		return ci;
	}

}
