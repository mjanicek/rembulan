package net.sandius.rembulan.core;

import net.sandius.rembulan.core.impl.Varargs;

public abstract class ObjectSink {

	protected boolean tailCall;

	protected ObjectSink() {
		tailCall = false;
	}

	public abstract int size();

	public boolean isTailCall() {
		return tailCall;
	}

	// resets tail call to false, size to 0
	public abstract void reset();

	public void markAsTailCall() {
		tailCall = true;
	}

	protected void resetTailCall() {
		tailCall = false;
	}

	public abstract void push(Object o);

	public void pushAll(Object[] a) {
		for (Object o : a) {
			push(o);
		}
	}

	public void setTo() {
		reset();
	}

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

	public void setToArray(Object[] a) {
		reset();
		for (Object o : a) {
			push(o);
		}
	}

	public void drop(int i) {
		if (i > 0) {
			setToArray(Varargs.from(toArray(), i));
		}
	}

	public void prepend(Object[] values) {
		Object[] old = toArray();
		reset();
		pushAll(values);
		pushAll(old);
	}

	public void tailCall(Object target) {
		setTo(target);
		markAsTailCall();
	}

	public void tailCall(Object target, Object arg1) {
		setTo(target, arg1);
		markAsTailCall();
	}

	public void tailCall(Object target, Object arg1, Object arg2) {
		setTo(target, arg1, arg2);
		markAsTailCall();
	}

	public void tailCall(Object target, Object arg1, Object arg2, Object arg3) {
		setTo(target, arg1, arg2, arg3);
		markAsTailCall();
	}

	public void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		setTo(target, arg1, arg2, arg3, arg4);
		markAsTailCall();
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
	}

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
