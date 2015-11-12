package net.sandius.rembulan.core;

public abstract class LuaCallInfo extends CallInfo {

	public LuaCallInfo(PreemptionContext context, ObjectStack objectStack, int base) {
		super(context, objectStack, base);
	}

//	public abstract int getMaxStackSize();  // TODO

}
