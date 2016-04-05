package net.sandius.rembulan.compiler.gen.block;

public class HookNode extends Linear {

	@Override
	public void emit(CodeEmitter e) {
		e._note("hook node ignored");
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
