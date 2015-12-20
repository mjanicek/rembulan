package net.sandius.rembulan.core.impl;

import java.util.ArrayList;

public class ArrayListObjectSink extends AbstractObjectSink {

	private final ArrayList<Object> buf;

	public ArrayListObjectSink() {
		super();
		buf = new ArrayList<>();
	}

	@Override
	public int size() {
		return buf.size();
	}

	@Override
	public void reset() {
		buf.clear();
		resetTailCall();
	}

	@Override
	public void push(Object o) {
		buf.add(o);
	}

	@Override
	public Object[] toArray() {
		return buf.toArray();
	}

	@Override
	public Object get(int idx) {
		return idx >= 0 && idx < buf.size() ? buf.get(idx) : null;
	}

}
