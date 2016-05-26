package net.sandius.rembulan.core;

public abstract class LuaState {

	public abstract Table nilMetatable();
	public abstract Table booleanMetatable();
	public abstract Table numberMetatable();
	public abstract Table stringMetatable();
	public abstract Table functionMetatable();
	public abstract Table threadMetatable();
	public abstract Table lightuserdataMetatable();

	public abstract Table setNilMetatable(Table table);
	public abstract Table setBooleanMetatable(Table table);
	public abstract Table setNumberMetatable(Table table);
	public abstract Table setStringMetatable(Table table);
	public abstract Table setThreadMetatable(Table table);
	public abstract Table setLightUserdataMetatable(Table table);

	public abstract ObjectSinkFactory objectSinkFactory();

	public ObjectSink newObjectSink() {
		return objectSinkFactory().newObjectSink();
	}

	public abstract UpvalueFactory upvalueFactory();

	public Upvalue newUpvalue(Object initialValue) {
		return upvalueFactory().newUpvalue(initialValue);
	}

	public abstract TableFactory tableFactory();

	public Table newTable(int array, int hash) {
		return tableFactory().newTable(array, hash);
	}

	public void checkCpu(int cost) throws ControlThrowable {
		preemptionContext().withdraw(cost);
	}

	public abstract PreemptionContext preemptionContext();

}
