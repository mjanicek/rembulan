package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class Coroutine {

	public final LuaState state;

	public final ObjectStack objectStack;
	public int top;

	public Coroutine(LuaState state, ObjectStack objectStack) {
		Check.notNull(state);
		Check.notNull(objectStack);

		this.state = state;
		this.objectStack = objectStack;
	}

	public LuaState getOwnerState() {
		return state;
	}

	public ObjectStack getObjectStack() {
		return objectStack;
	}

	@Override
	public String toString() {
		return "thread: 0x" + Integer.toHexString(hashCode());
	}

}
