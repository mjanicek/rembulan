package net.sandius.rembulan.util;

public abstract class AbstractObjectSink extends ObjectSink {

	protected boolean tailCall;

	protected AbstractObjectSink() {
		tailCall = false;
	}

	@Override
	public boolean isTailCall() {
		return tailCall;
	}

	protected void resetTailCall() {
		tailCall = false;
	}

	@Override
	public void markAsTailCall() {
		tailCall = true;
	}

}
