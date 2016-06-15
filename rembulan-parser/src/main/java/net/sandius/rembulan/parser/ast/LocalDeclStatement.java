package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class LocalDeclStatement extends BodyStatement {

	private final List<Name> names;
	private final List<Expr> initialisers;

	public LocalDeclStatement(Attributes attr, List<Name> names, List<Expr> initialisers) {
		super(attr);
		this.names = Check.notNull(names);
		if (names.isEmpty()) {
			throw new IllegalArgumentException("name list must not be empty");
		}
		this.initialisers = Check.notNull(initialisers);
	}

	public List<Name> names() {
		return names;
	}

	public List<Expr> initialisers() {
		return initialisers;
	}

	public LocalDeclStatement update(List<Name> names, List<Expr> initialisers) {
		if (this.names.equals(names) && this.initialisers.equals(initialisers)) {
			return this;
		}
		else {
			return new LocalDeclStatement(attributes(), names, initialisers);
		}
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
