package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.util.Check;

public class Variable {

	public static final Variable ENV = new Variable(Name.fromString("_ENV"));

	private final Name name;
	private final Ref ref;

	public Variable(Name name) {
		this.name = Check.notNull(name);
		this.ref = new Ref(this);
	}

	public Name name() {
		return name;
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
