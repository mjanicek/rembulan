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
		if (size < 3) {
			_0 = null;
			_1 = null;
		}
		else {
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
			case 2:
				_var.add(_0);
				_var.add(_1);
				_0 = null;
				_1 = null;
				// intentionally falling through

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
			default: return _var.toArray();
		}
	}

	@Override
	public Object get(int idx) {
		switch (idx) {
			case 0: return _0();
			case 1: return _1();
			default: return size < 3 ? null : _var.get(idx);
		}
	}

	@Override
	public Object _0() {
		return size < 3 ? _0 : _var.get(0);
	}

	@Override
	public Object _1() {
		return size < 3 ? _1 : _var.get(1);
	}

}
