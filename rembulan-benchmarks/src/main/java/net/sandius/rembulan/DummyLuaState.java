package net.sandius.rembulan;

import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class DummyLuaState extends LuaState {

	@Override
	public Table nilMetatable() {
		return null;
	}

	@Override
	public Table booleanMetatable() {
		return null;
	}

	@Override
	public Table numberMetatable() {
		return null;
	}

	@Override
	public Table stringMetatable() {
		return null;
	}

	@Override
	public Table functionMetatable() {
		return null;
	}

	@Override
	public Table threadMetatable() {
		return null;
	}

	@Override
	public Table lightuserdataMetatable() {
		return null;
	}

	@Override
	public boolean shouldPreemptNow() {
		return false;
	}

	@Override
	public Coroutine getCurrentCoroutine() {
		return null;
	}

}
