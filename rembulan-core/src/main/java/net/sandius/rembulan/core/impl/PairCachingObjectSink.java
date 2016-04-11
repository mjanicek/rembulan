package net.sandius.rembulan.core.impl;

import java.util.ArrayList;

public class PairCachingObjectSink extends AbstractObjectSink {

	private Object _0;
	private Object _1;

	private final ArrayList<Object> _var;

	private int size;

	public PairCachingObjectSink() {
		super();
		_var = new ArrayList<>();
	}

	@Override
	public int size() {
		return size;
	}

	protected void setCacheAndClearList(Object a, Object b) {
		_0 = a;
		_1 = b;
		if (size > 2) {
			_var.clear();
		}
		resetTailCall();
	}

	@Override
	public void reset() {
		setCacheAndClearList(null, null);
		size = 0;
	}

	@Override
	public void setTo(Object a) {
		setCacheAndClearList(a, null);
		size = 1;
	}

	@Override
	public void setTo(Object a, Object b) {
		setCacheAndClearList(a, b);
		size = 2;
	}

	@Override
	public void setTo(Object a, Object b, Object c) {
		setCacheAndClearList(a, b);
		_var.add(c);
		size = 3;
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d) {
		setCacheAndClearList(a, b);
		_var.add(c);
		_var.add(d);
		size = 4;
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d, Object e) {
		setCacheAndClearList(a, b);
		_var.add(c);
		_var.add(d);
		_var.add(e);
		size = 5;
	}

	@Override
	public void push(Object o) {
		switch (size++) {
			case 0:
				_0 = o;
				break;
			case 1:
				_1 = o;
				break;
			default:
				_var.add(o);
				break;
		}
	}

	private static final Object[] EMPTY_ARRAY = new Object[0];

	@Override
	public Object[] toArray() {
		switch (size) {
			case 0: return EMPTY_ARRAY;
			case 1: return new Object[] { _0 };
			case 2: return new Object[] { _0, _1 };
			default:
				Object[] result = new Object[size];
				result[0] = _0;
				result[1] = _1;
				Object[] tmp = _var.toArray();
				System.arraycopy(tmp, 0, result, 2, tmp.length);
				return result;
		}
	}

	@Override
	public Object[] tailAsArray() {
		switch (size) {
			case 0: throw new IllegalArgumentException();
			case 1: return EMPTY_ARRAY;
			case 2: return new Object[] { _1 };
			default:
				Object[] result = new Object[size - 1];
				result[0] = _1;
				Object[] tmp = _var.toArray();
				System.arraycopy(tmp, 0, result, 1, tmp.length);
				return result;
		}
	}

	@Override
	public Object get(int idx) {
		switch (idx) {
			case 0: return _0;
			case 1: return _1;
			default: return idx < size && idx > 1 ? _var.get(idx - 2) : null;
		}
	}

	@Override
	public Object _0() {
		return _0;
	}

	@Override
	public Object _1() {
		return _1;
	}

	@Override
	public Object _2() {
		return size > 2 ? _var.get(0) : null;
	}

	@Override
	public Object _3() {
		return size > 3 ? _var.get(1) : null;
	}

	@Override
	public Object _4() {
		return size > 4 ? _var.get(2) : null;
	}

}
