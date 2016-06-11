package net.sandius.rembulan.parser.ast;

public interface LiteralVisitor {

	void visitNil();

	void visitBoolean(boolean value);

	void visitInteger(long value);

	void visitFloat(double value);

	void visitString(String value);

}
