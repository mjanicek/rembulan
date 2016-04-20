package net.sandius.rembulan.core;

public abstract class LuaState {

	public abstract Table nilMetatable();
	public abstract Table booleanMetatable();
	public abstract Table numberMetatable();
	public abstract Table stringMetatable();
	public abstract Table functionMetatable();
	public abstract Table threadMetatable();
	public abstract Table lightuserdataMetatable();

	public abstract TableFactory tableFactory();

	public Table newTable(int array, int hash) {
		return tableFactory().newTable(array, hash);
	}

	public Upvalue newUpvalue(Object initialValue) {
		return new Upvalue(initialValue);
	}

	public abstract CoroutineFactory coroutineFactory();

	public Coroutine newCoroutine() {
		return coroutineFactory().newCoroutine(this);
	}

	public void checkCpu(int cost) throws ControlThrowable {
		preemptionContext().withdraw(cost);
	}

	public abstract PreemptionContext preemptionContext();

}
