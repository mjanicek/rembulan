package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.SourceInfo;
import net.sandius.rembulan.util.Check;

public class SourceElement<T> {

	public final SourceInfo src;
	public final T elem;

	public SourceElement(SourceInfo src, T elem) {
		this.src = Check.notNull(src);
		this.elem = Check.notNull(elem);
	}

	public static <T> SourceElement<T> of(SourceInfo src, T elem) {
		return new SourceElement<>(src, elem);
	}

	public SourceInfo sourceInfo() {
		return src;
	}

	public T element() {
		return elem;
	}

}
