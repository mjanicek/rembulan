package net.sandius.rembulan.core;

public interface ConstantsVisitor {

	void begin(int size);

	void visitNil(int idx);

	void visitBoolean(int idx, boolean value);

	void visitInteger(int idx, long value);

	void visitFloat(int idx, double value);

	void visitString(int idx, String value);

	void end();

}
