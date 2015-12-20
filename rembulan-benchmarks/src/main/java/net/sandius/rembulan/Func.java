package net.sandius.rembulan;

import net.sandius.rembulan.util.ObjectSink;

public interface Func {

	void invoke(ObjectSink result);

	void invoke(ObjectSink result, Object arg1);

	void invoke(ObjectSink result, Object arg1, Object arg2);

	void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3);

	void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4);

	void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5);

	void invoke(ObjectSink result, Object[] args);

	void resume(ObjectSink result, Object suspendedState);

}
