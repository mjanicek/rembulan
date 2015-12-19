package net.sandius.rembulan;

import java.util.ArrayList;

public class PairObjectSink extends ObjectSink {

	private Object _0;
	private Object _1;

	private final ArrayList<Object> _var;

	private int size;

	public PairObjectSink() {
		_var = new ArrayList<Object>();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void reset() {
		_0 = null;
		_1 = null;
		if (size > 2) {
			_var.clear();
		}
		size = 0;
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
	public Object get(int idx) {
		switch (idx) {
			case 0: return _0;
			case 1: return _1;
			default:
				int i = idx - 2;
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

}
