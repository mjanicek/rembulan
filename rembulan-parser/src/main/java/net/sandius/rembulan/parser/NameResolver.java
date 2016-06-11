package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.Name;

public interface NameResolver<T> {

	T resolve(Name n);

}
