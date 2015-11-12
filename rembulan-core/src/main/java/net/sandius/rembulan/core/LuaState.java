package net.sandius.rembulan.core;

public abstract class LuaState {

	public abstract Table nilMetatable();
	public abstract Table booleanMetatable();
	public abstract Table numberMetatable();
	public abstract Table stringMetatable();
	public abstract Table functionMetatable();
	public abstract Table threadMetatable();
	public abstract Table lightuserdataMetatable();

	private static final LuaState DUMMY_STATE = new LuaState() {

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

	};

	public static LuaState getCurrent() {
		return DUMMY_STATE;
	}

}
