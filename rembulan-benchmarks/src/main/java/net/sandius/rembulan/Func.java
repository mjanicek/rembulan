package net.sandius.rembulan;

import net.sandius.rembulan.util.ObjectSink;

public interface Func {

	void invoke(ObjectSink result);

	void invoke(ObjectSink result, Object a);

	void invoke(ObjectSink result, Object a, Object b);

	void invoke(ObjectSink result, Object a, Object b, Object c);

	void invoke(ObjectSink result, Object a, Object b, Object c, Object d);

	void invoke(ObjectSink result, Object a, Object b, Object c, Object d, Object e);

	void invoke(ObjectSink result, Object[] args);

	void resume(ObjectSink result, SuspendedState suspendedState);

}
