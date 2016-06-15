package net.sandius.rembulan.parser.ast;

public abstract class Statement extends SyntaxElement {

	protected Statement(SourceInfo src, Attributes attr) {
		super(src, attr);
	}

}
