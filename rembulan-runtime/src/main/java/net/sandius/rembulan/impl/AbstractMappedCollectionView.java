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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

abstract class AbstractMappedCollectionView<T> extends AbstractCollection<T> {

	private final Collection<?> collection;

	public AbstractMappedCollectionView(Collection<?> collection) {
		this.collection = collection;
	}

	protected abstract T map(Object object);

	class MappingIterator implements Iterator<T> {

		private final Iterator<?> iterator;

		private MappingIterator(Iterator<?> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public T next() {
			Object o = iterator.next();
			return map(o);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("read-only view");
		}

	}

	@Override
	public Iterator<T> iterator() {
		return new MappingIterator(collection.iterator());
	}

	@Override
	public int size() {
		return collection.size();
	}

}
