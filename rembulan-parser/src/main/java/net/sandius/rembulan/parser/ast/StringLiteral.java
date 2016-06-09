package net.sandius.rembulan.parser.ast;

public class StringLiteral implements Literal {

	private final String value;

	private StringLiteral(String value) {
		this.value = value;
	}

	public static StringLiteral fromString(String s) {
		throw new UnsupportedOperationException();  // TODO
	}

	public static StringLiteral fromName(Name n) {
		throw new UnsupportedOperationException();  // TODO
	}

	public String value() {
		return value;
	}

}
