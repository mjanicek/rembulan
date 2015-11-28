package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.util.Iterator;

public class Preempted extends ControlThrowable {

//	public static final Preempted INSTANCE = new Preempted();

	private CallInfo[] callStack;
	private int top;

	public Preempted() {
		callStack = new CallInfo[10];
		top = 0;
	}

	public static Preempted newInstance() {
		return new Preempted();
	}

	@Override
	public void push(CallInfo ci) {
		Check.notNull(ci);

		if (top >= callStack.length) {
			CallInfo[] newCallStack = new CallInfo[callStack.length * 2];
			System.arraycopy(callStack, 0, newCallStack, 0, top);
			callStack = newCallStack;
		}

		callStack[top++] = ci;
	}

	public CallInfo[] frames() {
		CallInfo[] result = new CallInfo[top];
		for (int i = 0; i < top; i++) {
			result[i] = callStack[top - i - 1];
		}
		return result;
	}

	@Override
	public Iterator<CallInfo> frameIterator() {
		return new Iterator<CallInfo>() {

			private int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < top;
			}

			@Override
			public CallInfo next() {
				return callStack[idx++];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
