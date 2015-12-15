package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

public class Preempted extends ControlThrowable {

	private final ArrayList<CallInfo> callStack;

	public Preempted() {
		callStack = new ArrayList<>();
	}

	public static Preempted newInstance() {
		return new Preempted();
	}

	@Override
	public void push(CallInfo ci) {
		Check.notNull(ci);
		callStack.add(ci);
	}

	@Override
	public ListIterator<CallInfo> frameIterator() {
		return Collections.unmodifiableList(callStack).listIterator();
	}

}
