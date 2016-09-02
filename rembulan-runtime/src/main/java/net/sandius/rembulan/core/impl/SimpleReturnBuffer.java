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

import net.sandius.rembulan.core.ReturnBuffer;

import java.util.Arrays;
import java.util.Collection;

/**
 * A return buffer implementation that stores values in an array freshly reallocated
 * on every assignment.
 */
public class SimpleReturnBuffer implements ReturnBuffer {

	private static final Object[] EMPTY_ARRAY = new Object[0];

	private Object[] values;
	private Object tailCallTarget;
	private boolean tailCall;

	public SimpleReturnBuffer() {
		this.values = EMPTY_ARRAY;
		this.tailCallTarget = null;
		this.tailCall = false;
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	public boolean isCall() {
		return tailCall;
	}

	@Override
	public Object getCallTarget() {
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

	private void setToTailCall(Object target, Object[] args) {
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
	public void setToContentsOf(Object[] a) {
		setReturn(Arrays.copyOf(a, a.length));
	}

	@Override
	public void setToContentsOf(Collection<?> collection) {
		setReturn(collection.toArray());
	}

	@Override
	public void setToCall(Object target) {
		setToTailCall(target, EMPTY_ARRAY);
	}

	@Override
	public void setToCall(Object target, Object arg1) {
		setToTailCall(target, new Object[] { arg1 });
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2) {
		setToTailCall(target, new Object[] { arg1, arg2 });
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3) {
		setToTailCall(target, new Object[] { arg1, arg2, arg3 });
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		setToTailCall(target, new Object[] { arg1, arg2, arg3, arg4 });
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		setToTailCall(target, new Object[] { arg1, arg2, arg3, arg4, arg5 });
	}

	@Override
	public void setToCallWithContentsOf(Object target, Object[] args) {
		setToTailCall(target, Arrays.copyOf(args, args.length));
	}

	@Override
	public void setToCallWithContentsOf(Object target, Collection<?> args) {
		setToTailCall(target, args.toArray());
	}

	@Override
	public Object[] getAsArray() {
		return Arrays.copyOf(values, values.length);
	}

	@Override
	public Object get(int idx) {
		return idx < values.length ? values[idx] : null;
	}

	@Override
	public Object get0() {
		return get(0);
	}

	@Override
	public Object get1() {
		return get(1);
	}

	@Override
	public Object get2() {
		return get(2);
	}

	@Override
	public Object get3() {
		return get(3);
	}

	@Override
	public Object get4() {
		return get(4);
	}

}
