package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Attributes {

	private final Map<String, Object> attribs;

	private static final Attributes EMPTY = new Attributes(Collections.<String, Object>emptyMap());

	private Attributes(Map<String, Object> attribs) {
		this.attribs = Check.notNull(attribs);
	}

	public static Attributes empty() {
		return EMPTY;
	}

	public Attributes with(String key, Object value) {
		Check.notNull(key);
		Check.notNull(value);

		if (attribs.get(key) == value) {
			return this;
		}
		else {
			Map<String, Object> as = new HashMap<>();
			as.putAll(attribs);
			as.put(key, value);
			return new Attributes(Collections.unmodifiableMap(as));
		}
	}

	public Object get(String key) {
		return attribs.get(key);
	}

	public <T> T get(String key, Class<T> clazz) {
		Object result = get(key);
		if (result != null && clazz.isAssignableFrom(result.getClass())) {
			@SuppressWarnings("unchecked")
			T r = (T) result;
			return r;
		}
		else {
			return null;
		}
	}

}
