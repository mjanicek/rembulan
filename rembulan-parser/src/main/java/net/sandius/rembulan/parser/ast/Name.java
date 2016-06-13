package net.sandius.rembulan.parser.ast;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Name {

	private final String value;

	private Name(String value) {
		this.value = checkValidName(value);
	}

	public static Name fromString(String s) {
		return new Name(s);
	}

	private static final Set<String> keywords;
	
	static {
		Set<String> ks = new HashSet<>();
		Collections.addAll(ks,
				"and", "break", "do", "else", "elseif", "end", "false", "for",
				"function", "goto", "if", "in", "local", "nil", "not", "or",
				"repeat", "return", "then", "true", "until", "while");
		keywords = Collections.unmodifiableSet(ks);
	}

	public static boolean isKeyword(String s) {
		return s != null && keywords.contains(s);
	}

	public static boolean isValidName(String s) {
		if (s == null || s.isEmpty() || isKeyword(s)) {
			return false;
		}

		char c = s.charAt(0);

		if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c != '_')) return false;

		for (int i = 1; i < s.length(); i++) {
			c = s.charAt(i);
			if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c != '_') && (c < '0' || c > '9')) return false;
		}

		return true;
	}

	public static String checkValidName(String s) {
		if (!isValidName(s)) {
			throw new IllegalArgumentException("Not a valid name: " + s);
		}
		else {
			return s;
		}
	}

	public String value() {
		return value;
	}

}
