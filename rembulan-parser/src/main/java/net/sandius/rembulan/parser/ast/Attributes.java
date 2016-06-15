package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Attributes {

	private final Map<Class<?>, Object> attribs;

	private static final Attributes EMPTY = new Attributes(Collections.<Class<?>, Object>emptyMap());

	private Attributes(Map<Class<?>, Object> attribs) {
		this.attribs = Check.notNull(attribs);
	}

	public static Attributes empty() {
		return EMPTY;
	}

	public static Attributes of(Object... objects) {
		if (objects.length > 0) {
			Map<Class<?>, Object> as = new HashMap<>();
			for (Object o : objects) {
				as.put(o.getClass(), o);
			}
			return new Attributes(Collections.unmodifiableMap(as));
		}
		else {
			return empty();
		}
	}

	public Attributes with(Object o) {
		Check.notNull(o);
		Class<?> clazz = o.getClass();

		if (Objects.equals(attribs.get(clazz), o)) {
			return this;
		}
		else {
			Map<Class<?>, Object> as = new HashMap<>();
			as.putAll(attribs);
			as.put(clazz, o);
			return new Attributes(Collections.unmodifiableMap(as));
		}
	}

	public <T> T get(Class<T> clazz) {
		Check.notNull(clazz);
		Object result = attribs.get(clazz);

		if (result != null) {
			if (clazz.isAssignableFrom(result.getClass())) {
				@SuppressWarnings("unchecked")
				T r = (T) result;
				return r;
			}
			else {
				throw new IllegalStateException("Illegal entry for " + clazz.getName());
			}
		}
		else {
			return null;
		}
	}

}
