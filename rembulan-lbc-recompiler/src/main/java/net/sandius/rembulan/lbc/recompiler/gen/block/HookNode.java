package net.sandius.rembulan.lbc.recompiler.gen.block;

import net.sandius.rembulan.lbc.recompiler.gen.CodeVisitor;

public class HookNode extends Linear {

	@Override
	public void emit(CodeVisitor visitor) {
		visitor._ignored(this);
	}

	public static class Call extends HookNode {

		@Override
		public String toString() {
			return "Hook.Call";
		}

	}

	public static class Return extends HookNode {

		@Override
		public String toString() {
			return "Hook.Return";
		}

	}

}
