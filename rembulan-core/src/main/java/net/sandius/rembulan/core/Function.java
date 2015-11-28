package net.sandius.rembulan.core;

public abstract class Function {

	public void call(Coroutine coroutine, int base, int returnBase) throws ControlThrowable {
		run(coroutine, base, returnBase, 0);
	}

	public void resume(Coroutine coroutine, CallInfo[] callStack, int index) throws ControlThrowable {
		if (index < callStack.length - 1) {
			int nextIndex = index + 1;
			CallInfo ci = callStack[nextIndex];
			ci.function.resume(coroutine, callStack, nextIndex);
		}

		assert (index == callStack.length - 1);  // this is now the top frame

		CallInfo ci = callStack[index];
		run(coroutine, ci.base, ci.returnAddr, ci.pc);
	}

	protected abstract void run(Coroutine coroutine, int base, int returnBase, int pc) throws ControlThrowable;

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}
