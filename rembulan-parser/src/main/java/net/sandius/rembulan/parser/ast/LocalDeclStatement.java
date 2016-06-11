package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.List;

public class LocalDeclStatement implements Statement {

	private final List<Name> names;
	private final List<Expr> initialisers;

	private LocalDeclStatement(List<Name> names, List<Expr> initialisers) {
		this.names = Check.notNull(names);
		if (names.isEmpty()) {
			throw new IllegalArgumentException("name list must not be empty");
		}
		this.initialisers = Check.notNull(initialisers);
	}

	public static final LocalDeclStatement of(List<Name> names, List<Expr> initialisers) {
		return new LocalDeclStatement(names, initialisers);
	}

	public static final LocalDeclStatement of(List<Name> names) {
		return of(names, Collections.<Expr>emptyList());
	}

	public static final LocalDeclStatement singleton(Name n) {
		Check.notNull(n);
		return of(Collections.singletonList(n));
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitLocalDecl(names, initialisers);
	}

}
