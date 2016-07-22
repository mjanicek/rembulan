package net.sandius.rembulan.lbc.recompiler.gen.block;

import net.sandius.rembulan.util.Check;

public class Predicates {

	public static <T extends Linear> IsClass<T> isClass(Class<T> clazz) {
		return new IsClass<T>(clazz);
	}

	public static class IsClass<T extends Linear> implements LinearPredicate {

		public final Class<T> clazz;

		public IsClass(Class<T> clazz) {
			this.clazz = Check.notNull(clazz);
		}

		@Override
		public boolean apply(Linear n) {
			return clazz.isAssignableFrom(n.getClass());
		}

	}

}
