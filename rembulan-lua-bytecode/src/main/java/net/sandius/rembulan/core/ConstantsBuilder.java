package net.sandius.rembulan.core;

import net.sandius.rembulan.util.GenericBuilder;

public interface ConstantsBuilder extends GenericBuilder<Constants> {

	void addNil();

	void addBoolean(boolean value);

	void addInteger(long value);

	void addFloat(double value);

	void addString(String value);

	interface Factory<T extends ConstantsBuilder> {
		T newBuilder();
	}

}
