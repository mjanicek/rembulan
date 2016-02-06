package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

public enum SlotType {

	ANY,
	NIL,
	BOOLEAN,
	NUMBER,
	NUMBER_INTEGER,
	NUMBER_FLOAT,
	STRING,
	TABLE,
	THREAD,
	FUNCTION;

	// TODO: number-as-string, string-as-number, true, false, actual constant values?

	public boolean isNumber() {
		return this == NUMBER || this == NUMBER_INTEGER || this == NUMBER_FLOAT;
	}

	public SlotType join(SlotType that) {
		Check.notNull(that);
		if (this == that) {
			return this;
		}
		else {
			if (this.isNumber() && that.isNumber()) {
				return NUMBER;
			}
			else {
				return ANY;
			}
		}
	}

	public static String toString(SlotType type) {
		switch (type) {
			case ANY: return "A";
			case NIL: return "-";
			case BOOLEAN: return "B";
			case NUMBER: return "N";
			case NUMBER_INTEGER: return "i";
			case NUMBER_FLOAT: return "f";
			case STRING: return "S";
			case FUNCTION: return "F";
			case TABLE: return "T";
			case THREAD: return "C";
			default:
			throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

}
