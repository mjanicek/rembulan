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

public class CachingReturnVectorFactory implements ReturnVectorFactory {

	public static final int DEFAULT_CACHE_SIZE = 2;

	public static final CachingReturnVectorFactory DEFAULT_INSTANCE = new CachingReturnVectorFactory(DEFAULT_CACHE_SIZE);

	private final int cacheSize;
	private final ReturnVectorFactory factory;

	public CachingReturnVectorFactory(int cacheSize) {
		ReturnVectorFactory fac = forSize(cacheSize);
		if (fac != null) {
			this.factory = fac;
			this.cacheSize = cacheSize;
		}
		else {
			this.factory = ArrayListReturnVector.FACTORY_INSTANCE;
			this.cacheSize = 0;
		}
	}

	private static ReturnVectorFactory forSize(int cacheSize) {
		switch (cacheSize) {
			case 2: return PairCachingReturnVector.FACTORY_INSTANCE;
			case 3: return TripleCachingReturnVector.FACTORY_INSTANCE;
			case 4: return QuintupleCachingReturnVector.FACTORY_INSTANCE;
			default: return null;
		}
	}

	public int getCacheSize() {
		return cacheSize;
	}

	@Override
	public ReturnVector newReturnVector() {
		return factory.newReturnVector();
	}

}
