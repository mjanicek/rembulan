package net.sandius.rembulan.core;

@Deprecated
public interface ConstantsVisitor {

	void visitBegin(int size);

	void visitEnd();

	void visitNil(int idx);

	void visitBoolean(int idx, boolean value);

	void visitInteger(int idx, long value);

	void visitFloat(int idx, double value);

	void visitString(int idx, String value);

}
