package net.sandius.rembulan.core;

public interface Registers {

	Object get(int idx);

	void set(int idx, Object object);

	int getTop();

	void setTop(int newTop);

	Registers from(int offset);

}
