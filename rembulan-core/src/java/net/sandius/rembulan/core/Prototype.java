package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;

public class Prototype {

	public final IntVector code;

	private Prototype(IntVector code) {
		Check.notNull(code);

		this.code = code;
	}

	public static class Builder {

		public IntVector code;

		private Builder() {
			this.code = null;
		}

		public Prototype build() {
			return new Prototype(code);
		}

	}

	public static Builder newBuilder() {
		return new Builder();
	}

}
