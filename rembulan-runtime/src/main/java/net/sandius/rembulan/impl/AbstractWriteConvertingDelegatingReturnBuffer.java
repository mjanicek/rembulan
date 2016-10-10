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

abstract class AbstractWriteConvertingDelegatingReturnBuffer extends DelegatingReturnBuffer {

	public AbstractWriteConvertingDelegatingReturnBuffer(ReturnBuffer buffer) {
		super(buffer);
	}

	protected abstract Object convert(Object object);

	protected abstract Object[] convert(Object[] array);

	protected abstract Collection<?> convert(Collection<?> collection);

	@Override
	public void setTo(Object a) {
		super.setTo(
				convert(a)
		);
	}

	@Override
	public void setTo(Object a, Object b) {
		super.setTo(
				convert(a),
				convert(b)
		);
	}

	@Override
	public void setTo(Object a, Object b, Object c) {
		super.setTo(
				convert(a),
				convert(b),
				convert(c)
		);
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d) {
		super.setTo(
				convert(a),
				convert(b),
				convert(c),
				convert(d)
		);
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d, Object e) {
		super.setTo(
				convert(a),
				convert(b),
				convert(c),
				convert(d),
				convert(e)
		);
	}

	@Override
	public void setToContentsOf(Object[] array) {
		super.setToContentsOf(
				convert(array)
		);
	}

	@Override
	public void setToContentsOf(Collection<?> collection) {
		super.setToContentsOf(
				convert(collection)
		);
	}

	@Override
	public void setToCall(Object target) {
		super.setToCall(
				convert(target)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1) {
		super.setToCall(
				convert(target),
				convert(arg1)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2) {
		super.setToCall(
				convert(target),
				convert(arg1),
				convert(arg2)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3) {
		super.setToCall(
				convert(target),
				convert(arg1),
				convert(arg2),
				convert(arg3)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		super.setToCall(
				convert(target),
				convert(arg1),
				convert(arg2),
				convert(arg3),
				convert(arg4)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		super.setToCall(
				convert(target),
				convert(arg1),
				convert(arg2),
				convert(arg3),
				convert(arg4),
				convert(arg5)
		);
	}

	@Override
	public void setToCallWithContentsOf(Object target, Object[] args) {
		super.setToCallWithContentsOf(
				convert(target),
				convert(args)
		);
	}

	@Override
	public void setToCallWithContentsOf(Object target, Collection<?> args) {
		super.setToCallWithContentsOf(
				convert(target),
				convert(args)
		);
	}

}
