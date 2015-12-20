package net.sandius.rembulan.util;

public abstract class ObjectSink {

	public abstract int size();

	public abstract boolean isTailCall();

	// resets tail call to false, size to 0
	public abstract void reset();

	public abstract void markAsTailCall();

	public abstract void push(Object o);

	public void setTo(Object a) {
		reset();
		push(a);
	}

	public void setTo(Object a, Object b) {
		reset();
		push(a);
		push(b);
	}

	public void setTo(Object a, Object b, Object c) {
		reset();
		push(a);
		push(b);
		push(c);
	}

	public void setTo(Object a, Object b, Object c, Object d) {
		reset();
		push(a);
		push(b);
		push(c);
		push(d);
	}

	public void setTo(Object a, Object b, Object c, Object d, Object e) {
		reset();
		push(a);
		push(b);
		push(c);
		push(d);
		push(e);
	}

	public abstract Object[] toArray();

	public Object[] tailAsArray() {
		Object[] tmp = toArray();
		Object[] result = new Object[tmp.length - 1];
		System.arraycopy(tmp, 1, result, 0, result.length);
		return result;
	}

	public abstract Object get(int idx);

	public Object _0() {
		return get(0);
	};

	public Object _1() {
		return get(1);
	}

	public Object _2() {
		return get(2);
	}

	public Object _3() {
		return get(3);
	}

	public Object _4() {
		return get(4);
	}

}
