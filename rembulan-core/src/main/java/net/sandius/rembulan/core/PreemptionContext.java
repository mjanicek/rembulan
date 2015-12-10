package net.sandius.rembulan.core;

public interface PreemptionContext {

	void account(int cost) throws ControlThrowable;

}
