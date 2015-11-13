package net.sandius.rembulan.core;

public abstract class Function {

//	public abstract Object[] invoke(Object[] args);

	public abstract void resume(CallInfo ci) throws ControlThrowable;

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}
