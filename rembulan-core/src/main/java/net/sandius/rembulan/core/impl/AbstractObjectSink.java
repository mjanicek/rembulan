package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ObjectSink;

public abstract class AbstractObjectSink implements ObjectSink {

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

	@Override
	public void pushAll(Object[] a) {
		for (Object o : a) {
			push(o);
		}
	}

	@Override
	public void setTo(Object a) {
		reset();
		push(a);
	}

	@Override
	public void setTo(Object a, Object b) {
		reset();
		push(a);
		push(b);
	}

	@Override
	public void setTo(Object a, Object b, Object c) {
		reset();
		push(a);
		push(b);
		push(c);
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d) {
		reset();
		push(a);
		push(b);
		push(c);
		push(d);
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d, Object e) {
		reset();
		push(a);
		push(b);
		push(c);
		push(d);
		push(e);
	}

	@Override
	public void drop(int i) {
		if (i > 0) {
			setToArray(Varargs.from(toArray(), i));
		}
	}

	@Override
	public void prepend(Object[] values) {
		Object[] old = toArray();
		reset();
		pushAll(values);
		pushAll(old);
	}

	@Override
	public void setToArray(Object[] a) {
		reset();
		for (Object o : a) {
			push(o);
		}
	}

	@Override
	public void tailCall(Object target) {
		setTo(target);
		markAsTailCall();
	}

	@Override
	public void tailCall(Object target, Object arg1) {
		setTo(target, arg1);
		markAsTailCall();
	}

	@Override
	public void tailCall(Object target, Object arg1, Object arg2) {
		setTo(target, arg1, arg2);
		markAsTailCall();
	}

	@Override
	public void tailCall(Object target, Object arg1, Object arg2, Object arg3) {
		setTo(target, arg1, arg2, arg3);
		markAsTailCall();
	}

	@Override
	public void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		setTo(target, arg1, arg2, arg3, arg4);
		markAsTailCall();
	}

	@Override
	public Object[] tailAsArray() {
		Object[] tmp = toArray();
		Object[] result = new Object[tmp.length - 1];
		System.arraycopy(tmp, 1, result, 0, result.length);
		return result;
	}

	@Override
	public Object _0() {
		return get(0);
	}

	@Override
	public Object _1() {
		return get(1);
	}

	@Override
	public Object _2() {
		return get(2);
	}

	@Override
	public Object _3() {
		return get(3);
	}

	@Override
	public Object _4() {
		return get(4);
	}

}
