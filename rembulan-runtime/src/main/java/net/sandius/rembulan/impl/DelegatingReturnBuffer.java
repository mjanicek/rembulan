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

package net.sandius.rembulan.impl;

import net.sandius.rembulan.runtime.ReturnBuffer;

import java.util.Collection;
import java.util.Objects;

class DelegatingReturnBuffer implements ReturnBuffer {

	protected final ReturnBuffer buffer;

	public DelegatingReturnBuffer(ReturnBuffer buffer) {
		this.buffer = Objects.requireNonNull(buffer);
	}

	@Override
	public int size() {
		return buffer.size();
	}

	@Override
	public boolean isCall() {
		return buffer.isCall();
	}

	@Override
	public Object getCallTarget() {
		return buffer.getCallTarget();
	}

	@Override
	public void setTo() {
		buffer.setTo();
	}

	@Override
	public void setTo(Object a) {
		buffer.setTo(a);
	}

	@Override
	public void setTo(Object a, Object b) {
		buffer.setTo(a, b);
	}

	@Override
	public void setTo(Object a, Object b, Object c) {
		buffer.setTo(a, b, c);
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d) {
		buffer.setTo(a, b, c, d);
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d, Object e) {
		buffer.setTo(a, b, c, d, e);
	}

	@Override
	public void setToContentsOf(Object[] array) {
		buffer.setToContentsOf(array);
	}

	@Override
	public void setToContentsOf(Collection<?> collection) {
		buffer.setToContentsOf(collection);
	}

	@Override
	public void setToCall(Object target) {
		buffer.setToCall(target);
	}

	@Override
	public void setToCall(Object target, Object arg1) {
		buffer.setToCall(target, arg1);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2) {
		buffer.setToCall(target, arg1, arg2);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3) {
		buffer.setToCall(target, arg1, arg2, arg3);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		buffer.setToCall(target, arg1, arg2, arg3, arg4);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		buffer.setToCall(target, arg1, arg2, arg3, arg4, arg5);
	}

	@Override
	public void setToCallWithContentsOf(Object target, Object[] args) {
		buffer.setToCallWithContentsOf(target, args);
	}

	@Override
	public void setToCallWithContentsOf(Object target, Collection<?> args) {
		buffer.setToCallWithContentsOf(target, args);
	}

	@Override
	public Object[] getAsArray() {
		return buffer.getAsArray();
	}

	@Override
	public Object get(int idx) {
		return buffer.get(idx);
	}

	@Override
	public Object get0() {
		return buffer.get0();
	}

	@Override
	public Object get1() {
		return buffer.get1();
	}

	@Override
	public Object get2() {
		return buffer.get2();
	}

	@Override
	public Object get3() {
		return buffer.get3();
	}

	@Override
	public Object get4() {
		return buffer.get4();
	}

}
