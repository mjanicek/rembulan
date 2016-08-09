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

import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.ObjectSinkFactory;

public class CachingObjectSinkFactory implements ObjectSinkFactory {

	public static final int DEFAULT_CACHE_SIZE = 2;

	public static final CachingObjectSinkFactory DEFAULT_INSTANCE = new CachingObjectSinkFactory(DEFAULT_CACHE_SIZE);

	private final int cacheSize;
	private final ObjectSinkFactory factory;

	public CachingObjectSinkFactory(int cacheSize) {
		ObjectSinkFactory fac = forSize(cacheSize);
		if (fac != null) {
			this.factory = fac;
			this.cacheSize = cacheSize;
		}
		else {
			this.factory = ArrayListObjectSink.FACTORY_INSTANCE;
			this.cacheSize = 0;
		}
	}

	private static ObjectSinkFactory forSize(int cacheSize) {
		switch (cacheSize) {
			case 2: return PairCachingObjectSink.FACTORY_INSTANCE;
			case 3: return TripleCachingObjectSink.FACTORY_INSTANCE;
			case 4: return QuintupleCachingObjectSink.FACTORY_INSTANCE;
			default: return null;
		}
	}

	public int getCacheSize() {
		return cacheSize;
	}

	@Override
	public ObjectSink newObjectSink() {
		return factory.newObjectSink();
	}

}
