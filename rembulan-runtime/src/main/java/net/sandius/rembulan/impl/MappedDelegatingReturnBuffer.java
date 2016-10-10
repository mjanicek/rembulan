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

class MappedDelegatingReturnBuffer extends DelegatingReturnBuffer {

	private final ReturnBuffers.ReadMapper readMapper;
	private final ReturnBuffers.WriteMapper writeMapper;

	public MappedDelegatingReturnBuffer(ReturnBuffer buffer, ReturnBuffers.ReadMapper readMapper, ReturnBuffers.WriteMapper writeMapper) {
		super(buffer);
		this.readMapper = readMapper;
		this.writeMapper = writeMapper;
	}

	private Object mapWrittenValue(Object object) {
		return writeMapper != null ? writeMapper.mapWrittenValue(object) : object;
	}

	private Object[] mapWrittenArray(Object[] array) {
		return writeMapper != null ? writeMapper.mapWrittenArray(array) : array;
	}

	private Collection<?> mapWrittenCollection(Collection<?> collection) {
		return writeMapper != null ? writeMapper.mapWrittenCollection(collection) : collection;
	}

	private Object mapReadValue(Object object) {
		return readMapper != null ? readMapper.mapReadValue(object) : object;
	}

	private Object[] mapReadArray(Object[] array) {
		return readMapper != null ? readMapper.mapReadArray(array) : array;
	}

	@Override
	public void setTo(Object a) {
		super.setTo(
				mapWrittenValue(a)
		);
	}

	@Override
	public void setTo(Object a, Object b) {
		super.setTo(
				mapWrittenValue(a),
				mapWrittenValue(b)
		);
	}

	@Override
	public void setTo(Object a, Object b, Object c) {
		super.setTo(
				mapWrittenValue(a),
				mapWrittenValue(b),
				mapWrittenValue(c)
		);
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d) {
		super.setTo(
				mapWrittenValue(a),
				mapWrittenValue(b),
				mapWrittenValue(c),
				mapWrittenValue(d)
		);
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d, Object e) {
		super.setTo(
				mapWrittenValue(a),
				mapWrittenValue(b),
				mapWrittenValue(c),
				mapWrittenValue(d),
				mapWrittenValue(e)
		);
	}

	@Override
	public void setToContentsOf(Object[] array) {
		super.setToContentsOf(
				mapWrittenArray(array)
		);
	}

	@Override
	public void setToContentsOf(Collection<?> collection) {
		super.setToContentsOf(
				mapWrittenCollection(collection)
		);
	}

	@Override
	public void setToCall(Object target) {
		super.setToCall(
				mapWrittenValue(target)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1) {
		super.setToCall(
				mapWrittenValue(target),
				mapWrittenValue(arg1)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2) {
		super.setToCall(
				mapWrittenValue(target),
				mapWrittenValue(arg1),
				mapWrittenValue(arg2)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3) {
		super.setToCall(
				mapWrittenValue(target),
				mapWrittenValue(arg1),
				mapWrittenValue(arg2),
				mapWrittenValue(arg3)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		super.setToCall(
				mapWrittenValue(target),
				mapWrittenValue(arg1),
				mapWrittenValue(arg2),
				mapWrittenValue(arg3),
				mapWrittenValue(arg4)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		super.setToCall(
				mapWrittenValue(target),
				mapWrittenValue(arg1),
				mapWrittenValue(arg2),
				mapWrittenValue(arg3),
				mapWrittenValue(arg4),
				mapWrittenValue(arg5)
		);
	}

	@Override
	public void setToCallWithContentsOf(Object target, Object[] args) {
		super.setToCallWithContentsOf(
				mapWrittenValue(target),
				mapWrittenArray(args)
		);
	}

	@Override
	public void setToCallWithContentsOf(Object target, Collection<?> args) {
		super.setToCallWithContentsOf(
				mapWrittenValue(target),
				mapWrittenCollection(args)
		);
	}

	@Override
	public Object getCallTarget() {
		return mapReadValue(super.getCallTarget());
	}

	@Override
	public Object[] getAsArray() {
		return mapReadArray(super.getAsArray());
	}

	@Override
	public Object get(int idx) {
		return mapReadValue(super.get(idx));
	}

	@Override
	public Object get0() {
		return mapReadValue(super.get0());
	}

	@Override
	public Object get1() {
		return mapReadValue(super.get1());
	}

	@Override
	public Object get2() {
		return mapReadValue(super.get2());
	}

	@Override
	public Object get3() {
		return mapReadValue(super.get3());
	}

	@Override
	public Object get4() {
		return mapReadValue(super.get4());
	}

}
