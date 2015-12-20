package net.sandius.rembulan;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.util.ObjectSink;

public interface Func {

	void invoke(ObjectSink result) throws ControlThrowable;

	void invoke(ObjectSink result, Object arg1) throws ControlThrowable;

	void invoke(ObjectSink result, Object arg1, Object arg2) throws ControlThrowable;

	void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3) throws ControlThrowable;

	void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable;

	void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable;

	void invoke(ObjectSink result, Object[] args) throws ControlThrowable;

	void resume(ObjectSink result, Object suspendedState) throws ControlThrowable;

}
