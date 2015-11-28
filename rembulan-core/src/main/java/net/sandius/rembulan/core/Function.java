package net.sandius.rembulan.core;

public abstract class Function {

	public void call(Registers self, Registers ret) throws ControlThrowable {
		run(self, ret, 0);
	}

	public void resume(CallInfo[] callStack, int index) throws ControlThrowable {
		if (index < callStack.length - 1) {
			int nextIndex = index + 1;
			callStack[nextIndex].function.resume(callStack, nextIndex);
		}

		assert (index == callStack.length - 1);  // this is now the top frame

		CallInfo ci = callStack[index];
		run(ci.self, ci.ret, ci.pc);
	}

	protected abstract void run(Registers self, Registers ret, int pc) throws ControlThrowable;

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}
