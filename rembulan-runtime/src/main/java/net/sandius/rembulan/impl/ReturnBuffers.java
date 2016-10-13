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
import net.sandius.rembulan.runtime.ReturnBufferFactory;

import java.util.Collection;
import java.util.Objects;

/**
 * Static factory for instantiating return buffers.
 *
 * <p>To obtain a new instance of the default return buffer implementation,
 * use {@link ReturnBuffers#newDefaultReturnBuffer()}.</p>
 */
public final class ReturnBuffers {

	private ReturnBuffers() {
		// not to be instantiated
	}

	/**
	 * Returns a new instance of the default return buffer implementation.
	 *
	 * <p>This implementation optimises access to the first two values in the
	 * buffer.</p>
	 *
	 * @return  a new instance of the default return buffer
	 */
	public static ReturnBuffer newDefaultReturnBuffer() {
		return new PairCachingReturnBuffer();
	}

	private static final ReturnBufferFactory DEFAULT_FACTORY_INSTANCE = new ReturnBufferFactory() {
		@Override
		public ReturnBuffer newInstance() {
			return newDefaultReturnBuffer();
		}
	};

	/**
	 * Returns a factory for constructing instances of the default return buffer implementation.
	 *
	 * @return  a factory for default return buffers
	 */
	public static ReturnBufferFactory defaultFactory() {
		return DEFAULT_FACTORY_INSTANCE;
	}

	interface ReadMapper {

		Object mapReadValue(Object object);

		Object[] mapReadArray(Object[] array);

	}

	interface WriteMapper {

		Object mapWrittenValue(Object object);

		Object[] mapWrittenArray(Object[] array);

		Collection<?> mapWrittenCollection(Collection<?> array);

	}

	abstract static class AbstractMapper implements ReturnBuffers.ReadMapper, ReturnBuffers.WriteMapper {

		protected abstract Object map(Object object);

		private Object[] mapArray(Object[] array, Object[] copy) {
			for (int i = 0; i < copy.length; i++) {
				copy[i] = map(array[i]);
			}
			return copy;
		}

		public Collection<Object> mapCollection(final Collection<?> collection) {
			return new AbstractMappedCollectionView<Object>(collection) {
				@Override
				protected Object map(Object object) {
					return AbstractMapper.this.map(object);
				}
			};
		}

		@Override
		public Object mapReadValue(Object object) {
			return map(object);
		}

		@Override
		public Object[] mapReadArray(Object[] array) {
			return mapArray(array, array);
		}

		@Override
		public Object mapWrittenValue(Object object) {
			return map(object);
		}

		@Override
		public Object[] mapWrittenArray(Object[] array) {
			return mapArray(array, new Object[array.length]);
		}

		@Override
		public Collection<?> mapWrittenCollection(Collection<?> array) {
			return mapCollection(array);
		}

	}

	private static final Canonicaliser CANONICALISER = new Canonicaliser();

	static class Canonicaliser extends ReturnBuffers.AbstractMapper {

		@Override
		protected Object map(Object object) {
			return Conversions.canonicalRepresentationOf(object);
		}

	}

	static ReturnBuffer mapped(ReturnBuffer buffer, ReadMapper reads, WriteMapper writes) {
		Objects.requireNonNull(buffer);
		if (reads == null && writes == null) {
			return buffer;
		}
		else {
			return new MappedDelegatingReturnBuffer(buffer, reads, writes);
		}
	}

	// TODO: clean up and make public
	static ReturnBufferFactory mapping(final ReturnBufferFactory factory, final ReadMapper reads, final WriteMapper writes) {
		Objects.requireNonNull(factory);
		return new ReturnBufferFactory() {
			@Override
			public ReturnBuffer newInstance() {
				return mapped(factory.newInstance(), reads, writes);
			}
		};
	}

	// TODO: clean up and make public
	static ReturnBufferFactory canonical(final ReturnBufferFactory factory, boolean reads, boolean writes) {
		return mapping(factory, reads ? CANONICALISER : null, writes ? CANONICALISER : null);
	}

}
