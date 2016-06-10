package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class Name {

	private final String value;

	private Name(String value) {
		this.value = checkValidName(value);
	}

	public static Name fromString(String s) {
		return new Name(s);
	}

	public static String checkValidName(String s) {
		Check.notNull(s);
		// TODO
		return s;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		return "(name " + value + ")";
	}

}
