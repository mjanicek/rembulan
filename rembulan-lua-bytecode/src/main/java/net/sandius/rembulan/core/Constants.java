package net.sandius.rembulan.core;

public interface Constants {

	int size();

	boolean isNil(int idx);

	boolean isBoolean(int idx);

	boolean isInteger(int idx);

	boolean isFloat(int idx);

	boolean isString(int idx);

	boolean getBoolean(int idx);

	long getInteger(int idx);

	double getFloat(int idx);

	String getString(int idx);

}
