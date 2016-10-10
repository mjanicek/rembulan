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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

class CanonicalisingDelegatingReturnBuffer extends AbstractWriteConvertingDelegatingReturnBuffer {

	public CanonicalisingDelegatingReturnBuffer(ReturnBuffer buffer) {
		super(buffer);
	}

	@Override
	protected Object convert(Object object) {
		return Conversions.canonicalRepresentationOf(object);
	}

	@Override
	protected Object[] convert(Object[] array) {
		return Conversions.copyAsCanonicalValues(array);
	}

	private class CanonicalisingIterator implements Iterator<Object> {

		private final Iterator<?> iterator;

		private CanonicalisingIterator(Iterator<?> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Object next() {
			Object o = iterator.next();
			return convert(o);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("read-only view");
		}

	}

	private class CanonicalisingView extends AbstractCollection<Object> {

		private final Collection<?> collection;

		public CanonicalisingView(Collection<?> collection) {
			this.collection = collection;
		}

		@Override
		public Iterator<Object> iterator() {
			return new CanonicalisingIterator(collection.iterator());
		}

		@Override
		public int size() {
			return collection.size();
		}

	}

	@Override
	protected Collection<?> convert(Collection<?> collection) {
		return new CanonicalisingView(collection);
	}

}
