package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.util.Check;

public abstract class Numeral implements Literal {

	private Numeral() {

	}

	public static Numeral fromString(String s) {
		Check.notNull(s);
		Number n = LuaFormat.tryParseNumeral(s);
		if (n == null) {
			throw new IllegalArgumentException("not a number: " + s);
		}
		return n instanceof Double || n instanceof Float
				? new FloatNumeral(n.doubleValue())
				: new IntegerNumeral(n.longValue());
	}

	public static class IntegerNumeral extends Numeral {

		private final long value;

		public IntegerNumeral(long value) {
			this.value = value;
		}

		public long value() {
			return value;
		}

		@Override
		public void accept(LiteralVisitor visitor) {
			visitor.visitInteger(value);
		}

	}

	public static class FloatNumeral extends Numeral {

		private final double value;

		public FloatNumeral(double value) {
			this.value = value;
		}

		public double value() {
			return value;
		}

		@Override
		public void accept(LiteralVisitor visitor) {
			visitor.visitFloat(value);
		}

	}

}
