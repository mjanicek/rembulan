package net.sandius.rembulan.parser.ast;

public class NilLiteral implements Literal {

	public static final NilLiteral INSTANCE = new NilLiteral();

	private NilLiteral() {
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
