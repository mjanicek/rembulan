package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.util.Check;

public class Variable {

	public static final Variable ENV = new Variable();

	private final Ref ref;

	public Variable() {
		this.ref = new Ref(this);
	}

	public Ref ref() {
		return ref;
	}

	public static class Ref {

		private final Variable var;

		public Ref(Variable var) {
			this.var = Check.notNull(var);
		}

		public Variable var() {
			return var;
		}

	}

}
