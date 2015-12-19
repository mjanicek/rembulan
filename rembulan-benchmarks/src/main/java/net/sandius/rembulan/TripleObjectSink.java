package net.sandius.rembulan;

import java.util.ArrayList;

public class TripleObjectSink extends ObjectSink {

	private Object _0;
	private Object _1;
	private Object _2;

	private final ArrayList<Object> _var;

	private int size;

	public TripleObjectSink() {
		_var = new ArrayList<Object>();
	}

	@Override
	public int size() {
		return size;
	}

	protected void setCacheAndClearList(Object a, Object b, Object c) {
		_0 = a;
		_1 = b;
		_2 = c;
		if (size > 3) {
			_var.clear();
		}
	}

	@Override
	public void reset() {
		setCacheAndClearList(null, null, null);
		size = 0;
	}

	@Override
	public void setTo(Object a) {
		setCacheAndClearList(a, null, null);
		size = 1;
	}

	@Override
	public void setTo(Object a, Object b) {
		setCacheAndClearList(a, b, null);
		size = 2;
	}

	@Override
	public void setTo(Object a, Object b, Object c) {
		setCacheAndClearList(a, b, c);
		size = 3;
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d) {
		setCacheAndClearList(a, b, c);
		_var.add(d);
		size = 4;
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
			case 2:
				_2 = o;
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
			case 3: return new Object[] { _0, _1, _2 };
			default:
				Object[] result = new Object[size];
				result[0] = _0;
				result[1] = _1;
				result[2] = _2;
				Object[] tmp = _var.toArray();
				System.arraycopy(tmp, 0, result, 3, tmp.length);
				return result;
		}
	}

	@Override
	public Object get(int idx) {
		switch (idx) {
			case 0: return _0;
			case 1: return _1;
			case 2: return _2;
			default:
				int i = idx - 3;
				return i >= 0 && i < _var.size() ? _var.get(i) : null;
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

	public Object _2() {
		return _2;
	}

}
