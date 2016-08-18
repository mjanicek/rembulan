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

import java.util.Arrays;

public class SimpleReturnVector implements ReturnVector {

	public static final ReturnVectorFactory FACTORY_INSTANCE = new ReturnVectorFactory() {
		@Override
		public ReturnVector newReturnVector() {
			return new SimpleReturnVector();
		}
	};

	private static final Object[] EMPTY_ARRAY = new Object[0];

	private Object[] values;
	private Object tailCallTarget;
	private boolean tailCall;

	public SimpleReturnVector() {
		this.values = EMPTY_ARRAY;
		this.tailCallTarget = null;
		this.tailCall = false;
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	public boolean isTailCall() {
		return tailCall;
	}

	@Override
	public Object getTailCallTarget() {
		if (!tailCall) {
			throw new IllegalStateException("Not a tail call");
		}
		else {
			return tailCallTarget;
		}
	}

	private void update(Object[] values, boolean tailCall, Object tailCallTarget) {
		this.values = values;
		this.tailCall = tailCall;
		this.tailCallTarget = tailCallTarget;
	}

	private void setReturn(Object[] values) {
		update(values, false, null);
	}

	private void setTailCall(Object target, Object[] args) {
		update(args, true, target);
	}

	@Override
	public void setTo() {
		setReturn(EMPTY_ARRAY);
	}

	@Override
	public void setTo(Object a) {
		setReturn(new Object[] {a});
	}

	@Override
	public void setTo(Object a, Object b) {
		setReturn(new Object[] {a, b});
	}

	@Override
	public void setTo(Object a, Object b, Object c) {
		setReturn(new Object[] {a, b, c});
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d) {
		setReturn(new Object[] {a, b, c, d});
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d, Object e) {
		setReturn(new Object[] {a, b, c, d, e});
	}

	@Override
	public void setToArray(Object[] a) {
		setReturn(Arrays.copyOf(a, a.length));
	}

	@Override
	public void tailCall(Object target) {
		setTailCall(target, EMPTY_ARRAY);
	}

	@Override
	public void tailCall(Object target, Object arg1) {
		setTailCall(target, new Object[] { arg1 });
	}

	@Override
	public void tailCall(Object target, Object arg1, Object arg2) {
		setTailCall(target, new Object[] { arg1, arg2 });
	}

	@Override
	public void tailCall(Object target, Object arg1, Object arg2, Object arg3) {
		setTailCall(target, new Object[] { arg1, arg2, arg3 });
	}

	@Override
	public void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		setTailCall(target, new Object[] { arg1, arg2, arg3, arg4 });
	}

	@Override
	public void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		setTailCall(target, new Object[] { arg1, arg2, arg3, arg4, arg5 });
	}

	@Override
	public void tailCall(Object target, Object[] args) {
		setTailCall(target, Arrays.copyOf(args, args.length));
	}

	@Override
	public Object[] toArray() {
		return Arrays.copyOf(values, values.length);
	}

	@Override
	public Object get(int idx) {
		return idx < values.length ? values[idx] : null;
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
