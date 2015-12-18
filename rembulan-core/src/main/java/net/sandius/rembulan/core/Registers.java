package net.sandius.rembulan.core;

public interface Registers {

	int size();

	void push(Object object);

	Object get(int idx);

	void set(int idx, Object object);

	int getTop();

	void setTop(int newTop);

	ReturnTarget returnTargetFrom(int offset);

}
