package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.ObjectSinkFactory;

import java.util.ArrayList;

public class ArrayListObjectSink extends ObjectSink {

	public static final ObjectSinkFactory FACTORY_INSTANCE = new ObjectSinkFactory() {
		@Override
		public ObjectSink newObjectSink() {
			return new ArrayListObjectSink();
		}
	};

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
