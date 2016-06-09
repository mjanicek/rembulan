package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class StringLiteral implements Literal {

	private final String value;

	private StringLiteral(String value) {
		this.value = value;
	}

	private static String stringValueOf(String s) {
		Check.notNull(s);

		StringBuilder bld = new StringBuilder();

		int i = 0;

		boolean dbl = s.charAt(0) == '"';

		// ignore the initial quotation mark
		i += 1;

		while (i < s.length() - 1) {
			// TODO: escapes
			bld.append(s.charAt(i));
			i += 1;
		}

		return bld.toString();
	}

	public static StringLiteral fromString(String s) {
		return new StringLiteral(stringValueOf(s));
	}

	public static StringLiteral fromName(Name n) {
		return new StringLiteral(n.value());
	}

	public String value() {
		return value;
	}

}
