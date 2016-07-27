package net.sandius.rembulan.core;

public interface PreemptionContext {

	boolean withdraw(int cost);

	boolean isPreempted();

	class Always implements PreemptionContext {

		public static final Always INSTANCE = new Always();

		@Override
		public boolean withdraw(int cost) {
			return true;
		}

		@Override
		public boolean isPreempted() {
			return true;
		}

	}

	class Never implements PreemptionContext {

		public static final Never INSTANCE = new Never();

		@Override
		public boolean withdraw(int cost) {
			return false;
		}

		@Override
		public boolean isPreempted() {
			return false;
		}

	}

}
