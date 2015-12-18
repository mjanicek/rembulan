package net.sandius.rembulan.core;

public interface PreemptionContext {

	void withdraw(int cost) throws ControlThrowable;

}
