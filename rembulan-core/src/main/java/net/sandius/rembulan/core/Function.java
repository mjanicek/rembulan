package net.sandius.rembulan.core;

public abstract class Function {

//	public abstract Object[] invoke(Object[] args);

	public abstract void resume(PreemptionContext context, ObjectStack objectStack, int base, int returnBase, int pc) throws ControlThrowable;

	public void resume(CallInfo ci) throws ControlThrowable {
		resume(ci.context, ci.objectStack, ci.base, ci.returnAddr, ci.pc);
	}

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}
