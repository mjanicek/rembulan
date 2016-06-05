package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaType;

public abstract class LuaState implements MetatableProvider, TableFactory, UpvalueFactory, ObjectSinkFactory {

	public abstract Table nilMetatable();
	public abstract Table booleanMetatable();
	public abstract Table numberMetatable();
	public abstract Table stringMetatable();
	public abstract Table functionMetatable();
	public abstract Table threadMetatable();
	public abstract Table lightUserdataMetatable();

	@Override
	public Table getMetatable(Object o) {
		if (o instanceof LuaObject) {
			return ((LuaObject) o).getMetatable();
		}
		else {
			LuaType type = Values.typeOf(o);
			switch (type) {
				case NIL: return nilMetatable();
				case BOOLEAN: return booleanMetatable();
				case NUMBER: return numberMetatable();
				case STRING: return stringMetatable();
				case FUNCTION: return functionMetatable();
				case THREAD: return threadMetatable();
				case USERDATA: return lightUserdataMetatable();
				default: throw new IllegalStateException("Illegal type: " + type);
			}
		}
	}

	public abstract Table setNilMetatable(Table table);
	public abstract Table setBooleanMetatable(Table table);
	public abstract Table setNumberMetatable(Table table);
	public abstract Table setStringMetatable(Table table);
	public abstract Table setFunctionMetatable(Table table);
	public abstract Table setThreadMetatable(Table table);
	public abstract Table setLightUserdataMetatable(Table table);

	public Table setMetatable(Object o, Table table) {
		if (o instanceof LuaObject) {
			return ((LuaObject) o).setMetatable(table);
		}
		else {
			LuaType type = Values.typeOf(o);
			switch (type) {
				case NIL: return setNilMetatable(table);
				case BOOLEAN: return setBooleanMetatable(table);
				case NUMBER: return setNumberMetatable(table);
				case STRING: return setStringMetatable(table);
				case FUNCTION: return setFunctionMetatable(table);
				case THREAD: return setThreadMetatable(table);
				case USERDATA: return setLightUserdataMetatable(table);
				default: throw new IllegalStateException("Illegal type: " + type);
			}
		}
	}

	public abstract ObjectSinkFactory objectSinkFactory();

	@Override
	public ObjectSink newObjectSink() {
		return objectSinkFactory().newObjectSink();
	}

	public abstract UpvalueFactory upvalueFactory();

	@Override
	public Upvalue newUpvalue(Object initialValue) {
		return upvalueFactory().newUpvalue(initialValue);
	}

	public abstract TableFactory tableFactory();

	@Override
	public Table newTable() {
		return tableFactory().newTable();
	}

	@Override
	public Table newTable(int array, int hash) {
		return tableFactory().newTable(array, hash);
	}

	public void checkCpu(int cost) throws ControlThrowable {
		preemptionContext().withdraw(cost);
	}

	public abstract PreemptionContext preemptionContext();

}
