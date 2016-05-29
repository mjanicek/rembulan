package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaType;

public abstract class LuaState implements MetatableProvider {

	public abstract Table nilMetatable();
	public abstract Table booleanMetatable();
	public abstract Table numberMetatable();
	public abstract Table stringMetatable();
	public abstract Table functionMetatable();
	public abstract Table threadMetatable();
	public abstract Table lightuserdataMetatable();

	@Override
	public Table getMetatable(Object o) {
		if (o instanceof LuaObject) {
			return ((LuaObject) o).getMetatable();
		}
		else {
			LuaType type = Value.typeOf(o);
			switch (type) {
				case NIL: return nilMetatable();
				case BOOLEAN: return booleanMetatable();
				case LIGHTUSERDATA: return lightuserdataMetatable();
				case NUMBER: return numberMetatable();
				case STRING: return stringMetatable();
				case FUNCTION: return functionMetatable();
				case THREAD: return threadMetatable();
				default: throw new IllegalStateException("Illegal type: " + type);
			}
		}
	}

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
