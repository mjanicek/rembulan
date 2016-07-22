package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.util.Check;

public class UpVar extends AbstractVar {

	private final Name name;

	public UpVar(Name name) {
		this.name = Check.notNull(name);
	}

	public Name name() {
		return name;
	}

	@Override
	public String toString() {
		return "^" + name.value();
	}

}
