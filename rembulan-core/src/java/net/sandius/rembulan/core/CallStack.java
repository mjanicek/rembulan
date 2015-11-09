package net.sandius.rembulan.core;

public class CallStack {

	private int top;
	private final CallInfo[] call;

	public CallStack(int maxSize) {
		if (maxSize < 1) {
			throw new IllegalArgumentException("Call stack max size must be at least 1");
		}

		this.call = new CallInfo[maxSize];
		this.top = -1;
	}

	public void push(CallInfo ci) {
		if (top + 1 >= call.length) {
			throw new IllegalStateException("Call stack full");
		}

		call[++top] = ci;
	}

	public CallInfo pop() {
		if (top < 0) {
			throw new IllegalStateException("Call stack empty");
		}

		CallInfo result = call[top];
		call[top--] = null;
		return result;
	}

	// returns null if stack empty
	public CallInfo peek() {
		return top >= 0 ? call[top] : null;
	}

}
