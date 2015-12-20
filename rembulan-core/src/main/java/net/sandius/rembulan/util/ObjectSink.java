package net.sandius.rembulan.util;

public interface ObjectSink {

	int size();

	boolean isTailCall();

	// resets tail call to false, size to 0
	void reset();

	void markAsTailCall();

	void push(Object o);

	void setTo(Object a);

	void setTo(Object a, Object b);

	void setTo(Object a, Object b, Object c);

	void setTo(Object a, Object b, Object c, Object d);

	void setTo(Object a, Object b, Object c, Object d, Object e);

	Object[] toArray();

	Object[] tailAsArray();

	Object get(int idx);

	Object _0();
	
	Object _1();

	Object _2();

	Object _3();

	Object _4();

}
