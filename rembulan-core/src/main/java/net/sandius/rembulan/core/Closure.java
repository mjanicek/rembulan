package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public abstract class Closure extends Function {

	protected final Prototype prototype;

	public Closure(Prototype prototype) {
		Check.notNull(prototype);
		this.prototype = prototype;
	}

}
