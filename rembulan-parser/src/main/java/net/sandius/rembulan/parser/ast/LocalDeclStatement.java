package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.List;

public class LocalDeclStatement extends BodyStatement {

	private final List<Name> names;
	private final List<Expr> initialisers;

	private LocalDeclStatement(SourceInfo src, List<Name> names, List<Expr> initialisers) {
		super(src);
		this.names = Check.notNull(names);
		if (names.isEmpty()) {
			throw new IllegalArgumentException("name list must not be empty");
		}
		this.initialisers = Check.notNull(initialisers);
	}

	public static final LocalDeclStatement of(SourceInfo src, List<Name> names, List<Expr> initialisers) {
		return new LocalDeclStatement(src, names, initialisers);
	}

	public static final LocalDeclStatement of(SourceInfo src, List<Name> names) {
		return of(src, names, Collections.<Expr>emptyList());
	}

	public static final LocalDeclStatement singleton(SourceInfo src, Name n) {
		return of(src, Collections.singletonList(Check.notNull(n)));
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
			return new LocalDeclStatement(sourceInfo(), names, initialisers);
		}
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public BodyStatement acceptTransformer(Transformer tf) {
		return tf.transform(this);
	}

}
