package net.sandius.rembulan;

import java.util.ArrayList;

public class SimpleObjectSink extends ObjectSink {

	private final ArrayList<Object> buf;

	public SimpleObjectSink() {
		buf = new ArrayList<Object>();
	}

	@Override
	public int size() {
		return buf.size();
	}

	@Override
	public void reset() {
		buf.clear();
	}

	@Override
	public void setTo(Object a) {
		buf.clear();
		buf.add(a);
	}

	@Override
	public void setTo(Object a, Object b) {
		buf.clear();
		buf.add(a);
		buf.add(b);
	}

	@Override
	public void setTo(Object a, Object b, Object c) {
		buf.clear();
		buf.add(a);
		buf.add(b);
		buf.add(c);
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d) {
		buf.clear();
		buf.add(a);
		buf.add(b);
		buf.add(c);
		buf.add(d);
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

	@Override
	public Object _0() {
		return buf.size() < 1 ? null : buf.get(0);
	}

	@Override
	public Object _1() {
		return buf.size() < 2 ? null : buf.get(1);
	}
}
