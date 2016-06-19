package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.List;

public abstract class VList {

	public static class Fixed extends VList {

		private final List<Temp> addrs;

		public Fixed(List<Temp> addrs) {
			this.addrs = Check.notNull(addrs);
		}

		public List<Temp> addrs() {
			return addrs;
		}

	}

	public static class Multi extends VList {

		public Multi() {
			throw new UnsupportedOperationException();  // TODO
		}

	}

}
