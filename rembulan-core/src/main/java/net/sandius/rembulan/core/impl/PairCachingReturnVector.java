/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ReturnVector;
import net.sandius.rembulan.core.ReturnVectorFactory;

public class PairCachingReturnVector implements ReturnVector {

	public static final ReturnVectorFactory FACTORY_INSTANCE = new ReturnVectorFactory() {
		@Override
		public ReturnVector newReturnVector() {
			return new PairCachingReturnVector();
		}
	};

	private static final Object[] EMPTY_ARRAY = new Object[0];

	public static final int MIN_BUF_SIZE = 3;

	// by default, handle up to 10 values without reallocating
	private static final int DEFAULT_PREFERRED_BUF_SIZE = 8;

	// size to trim down to as soon as possible
	private final int preferredBufSize;

	private int size;
	private Object _0;
	private Object _1;
	private Object[] _buf;

	private Object tailCallTarget;
	private boolean tailCall;

	public PairCachingReturnVector(int preferredBufSize) {
		if (preferredBufSize < MIN_BUF_SIZE) {
			throw new IllegalArgumentException("Preferred array size must be at least " + MIN_BUF_SIZE);
		}

		this.preferredBufSize = preferredBufSize;

		this._0 = null;
		this._1 = null;
		this._buf = new Object[preferredBufSize];
		this.size = 0;

		this.tailCallTarget = null;
		this.tailCall = false;
	}

	public PairCachingReturnVector() {
		this(DEFAULT_PREFERRED_BUF_SIZE);
	}

	@Override
	public boolean isTailCall() {
		return tailCall;
	}

	@Override
	public Object getTailCallTarget() {
		if (tailCall) {
			return tailCallTarget;
		}
		else {
			throw new IllegalStateException("Not a tail call");
		}
	}

	protected void unsetTailCall() {
		tailCall = false;
		tailCallTarget = null;
	}

	protected void setTailCall(Object target) {
		tailCall = true;
		tailCallTarget = target;
	}

	@Override
	public int size() {
		return size;
	}

	private void ensureBufSizeAtLeast(int sizeAtLeast) {
		int sz = sizeAtLeast > preferredBufSize ? sizeAtLeast : preferredBufSize;

		if (sz != _buf.length) {
			// resize: initialised to nulls, we're done
			_buf = new Object[sz];
		}
		else {
			// new size still fits, null everything between oldSize and newSize
			int oldSize = size - 2;
			for (int i = sizeAtLeast; i < oldSize; i++) {
				_buf[i] = null;
			}
		}
	}

	private void _set(Object a, Object b, int bufSize, int size) {
		_0 = a;
		_1 = b;
		ensureBufSizeAtLeast(bufSize);
		this.size = size;
	}

	private void _setArray(Object[] a) {
		int sz = a.length;

		int asz = sz - 2;
		if (asz > 0) {
			// copy contents to buffer
			ensureBufSizeAtLeast(asz);
			System.arraycopy(a, 2, _buf, 0, asz);
		}
		else {
			// just clear the buffer
			ensureBufSizeAtLeast(0);
		}

		Object o0 = null, o1 = null;
		switch (sz) {
			default:
			case 2: o1 = a[1];
			case 1: o0 = a[0];
			case 0:
		}
		_0 = o0;
		_1 = o1;

		size = sz;
	}

	@Override
	public void setTo() {
		unsetTailCall();
		_set(null, null, 0, 0);
	}

	@Override
	public void setTo(Object a) {
		unsetTailCall();
		_set(a, null, 0, 1);
	}

	@Override
	public void setTo(Object a, Object b) {
		unsetTailCall();
		_set(a, b, 0, 2);
	}

	@Override
	public void setTo(Object a, Object b, Object c) {
		unsetTailCall();
		_set(a, b, 1, 3);
		_buf[0] = c;
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d) {
		unsetTailCall();
		_set(a, b, 2, 4);
		_buf[0] = c;
		_buf[1] = d;
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d, Object e) {
		unsetTailCall();
		_set(a, b, 3, 5);
		_buf[0] = c;
		_buf[1] = d;
		_buf[2] = e;
	}

	@Override
	public void setToArray(Object[] a) {
		unsetTailCall();
		_setArray(a);
	}

	@Override
	public void tailCall(Object target) {
		setTailCall(target);
		_set(null, null, 0, 0);
	}

	@Override
	public void tailCall(Object target, Object arg1) {
		setTailCall(target);
		_set(arg1, null, 0, 1);
	}

	@Override
	public void tailCall(Object target, Object arg1, Object arg2) {
		setTailCall(target);
		_set(arg1, arg2, 0, 2);
	}

	@Override
	public void tailCall(Object target, Object arg1, Object arg2, Object arg3) {
		setTailCall(target);
		_set(arg1, arg2, 1, 3);
		_buf[0] = arg3;
	}

	@Override
	public void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		setTailCall(target);
		_set(arg1, arg2, 2, 4);
		_buf[0] = arg3;
		_buf[1] = arg4;
	}

	@Override
	public void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		setTailCall(target);
		_set(arg1, arg2, 3, 5);
		_buf[0] = arg3;
		_buf[1] = arg4;
		_buf[2] = arg5;
	}

	@Override
	public void tailCall(Object target, Object[] args) {
		setTailCall(target);
		_setArray(args);
	}

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
				System.arraycopy(_buf, 0, result, 2, size - 2);
				return result;
		}
	}

	@Override
	public Object get(int idx) {
		if (idx < 0) {
			return null;
		}
		else {
			switch (idx) {
				case 0:  return _0;
				case 1:  return _1;
				default: return idx < size ? _buf[idx - 2] : null;
			}
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
		// assuming buf is always big enough
		return _buf[0];
	}

	@Override
	public Object _3() {
		// assuming buf is always big enough
		return _buf[1];
	}

	@Override
	public Object _4() {
		// assuming buf is always big enough
		return _buf[2];
	}

}
