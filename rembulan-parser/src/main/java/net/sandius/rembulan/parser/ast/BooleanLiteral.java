package net.sandius.rembulan.parser.ast;

public class BooleanLiteral implements Literal {

	public static final BooleanLiteral TRUE = new BooleanLiteral(true);
	public static final BooleanLiteral FALSE = new BooleanLiteral(false);

	private final boolean value;

	private BooleanLiteral(boolean value) {
		this.value = value;
	}

	public BooleanLiteral fromBoolean(boolean b) {
		return b ? TRUE : FALSE;
	}

	public boolean value() {
		return value;
	}

}
