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

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.runtime.ReturnBuffer;

import java.util.Arrays;
import java.util.Collection;

class CanonicalisingReturnBufferWrapper extends DelegatingReturnBuffer {

	public CanonicalisingReturnBufferWrapper(ReturnBuffer buffer) {
		super(buffer);
	}

	@Override
	public void setTo(Object a) {
		super.setTo(
				Conversions.canonicalRepresentationOf(a)
		);
	}

	@Override
	public void setTo(Object a, Object b) {
		super.setTo(
				Conversions.canonicalRepresentationOf(a),
				Conversions.canonicalRepresentationOf(b)
		);
	}

	@Override
	public void setTo(Object a, Object b, Object c) {
		super.setTo(
				Conversions.canonicalRepresentationOf(a),
				Conversions.canonicalRepresentationOf(b),
				Conversions.canonicalRepresentationOf(c)
		);
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d) {
		super.setTo(
				Conversions.canonicalRepresentationOf(a),
				Conversions.canonicalRepresentationOf(b),
				Conversions.canonicalRepresentationOf(c),
				Conversions.canonicalRepresentationOf(d)
		);
	}

	@Override
	public void setTo(Object a, Object b, Object c, Object d, Object e) {
		super.setTo(
				Conversions.canonicalRepresentationOf(a),
				Conversions.canonicalRepresentationOf(b),
				Conversions.canonicalRepresentationOf(c),
				Conversions.canonicalRepresentationOf(d),
				Conversions.canonicalRepresentationOf(e)
		);
	}

	@Override
	public void setToContentsOf(Object[] array) {
		super.setToContentsOf(
				Conversions.copyAsCanonicalValues(array)
		);
	}

	@Override
	public void setToContentsOf(Collection<?> collection) {
		super.setToContentsOf(
				Arrays.asList(Conversions.toCanonicalValues(collection.toArray()))  // FIXME: use a view!
		);
	}

	@Override
	public void setToCall(Object target) {
		super.setToCall(
				Conversions.canonicalRepresentationOf(target)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1) {
		super.setToCall(
				Conversions.canonicalRepresentationOf(target),
				Conversions.canonicalRepresentationOf(arg1)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2) {
		super.setToCall(
				Conversions.canonicalRepresentationOf(target),
				Conversions.canonicalRepresentationOf(arg1),
				Conversions.canonicalRepresentationOf(arg2)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3) {
		super.setToCall(
				Conversions.canonicalRepresentationOf(target),
				Conversions.canonicalRepresentationOf(arg1),
				Conversions.canonicalRepresentationOf(arg2),
				Conversions.canonicalRepresentationOf(arg3)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		super.setToCall(
				Conversions.canonicalRepresentationOf(target),
				Conversions.canonicalRepresentationOf(arg1),
				Conversions.canonicalRepresentationOf(arg2),
				Conversions.canonicalRepresentationOf(arg3),
				Conversions.canonicalRepresentationOf(arg4)
		);
	}

	@Override
	public void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		super.setToCall(
				Conversions.canonicalRepresentationOf(target),
				Conversions.canonicalRepresentationOf(arg1),
				Conversions.canonicalRepresentationOf(arg2),
				Conversions.canonicalRepresentationOf(arg3),
				Conversions.canonicalRepresentationOf(arg4),
				Conversions.canonicalRepresentationOf(arg5)
		);
	}

	@Override
	public void setToCallWithContentsOf(Object target, Object[] args) {
		super.setToCallWithContentsOf(
				Conversions.canonicalRepresentationOf(target),
				Conversions.copyAsCanonicalValues(args)
		);
	}

	@Override
	public void setToCallWithContentsOf(Object target, Collection<?> args) {
		super.setToCallWithContentsOf(
				Conversions.canonicalRepresentationOf(target),
				Arrays.asList(Conversions.copyAsCanonicalValues(args.toArray()))  // FIXME: use a view!
		);
	}

}
