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
