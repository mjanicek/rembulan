package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.Map;

public class Bindings {

	private final Bindings parent;
	private final Map<Name, Variable> locals;

	private Bindings(Bindings parent, Map<Name, Variable> locals) {
		this.parent = parent;
		this.locals = Check.notNull(locals);
	}

	public static Bindings root() {
		return new Bindings(null, Collections.<Name, Variable>emptyMap());
	}

	public Bindings nested() {
		return new Bindings(this, Collections.<Name, Variable>emptyMap());
	}

	public void addLocal(Name n, Variable var) {
		Check.notNull(n);
		Check.notNull(var);
		locals.put(n, var);
	}

	// returns null if n is a global variable
	public Variable resolve(Name n) {
		Check.notNull(n);
		Variable v = locals.get(n);
		if (v != null) {
			return v;
		}
		else {
			return parent != null ? parent.resolve(n) : null;
		}
	}

}
