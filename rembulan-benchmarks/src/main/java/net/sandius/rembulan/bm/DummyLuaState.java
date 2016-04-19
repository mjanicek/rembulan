package net.sandius.rembulan.bm;

import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.impl.DefaultLuaState;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class DummyLuaState extends DefaultLuaState {

	public DummyLuaState() {
		super(PreemptionContext.Never.INSTANCE);
	}

}
