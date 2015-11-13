package net.sandius.rembulan.core;

public abstract class Function {

//	public abstract Object[] invoke(Object[] args);

	public abstract void resume(Coroutine coroutine, int base, int returnBase, int pc) throws ControlThrowable;

	public void resume(Coroutine coroutine, CallInfo ci) throws ControlThrowable {
		resume(coroutine, ci.base, ci.returnAddr, ci.pc);
	}

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}
