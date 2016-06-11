package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.List;

// FIXME: is this a good name? it isn't an instance of Literal...
public class FunctionLiteral {

	private final Params params;
	private final Block block;

	public FunctionLiteral(Params params, Block block) {
		this.params = Check.notNull(params);
		this.block = Check.notNull(block);
	}

	public Params params() {
		return params;
	}

	public Block block() {
		return block;
	}

	public static class Params {

		private final List<Name> names;
		private final boolean vararg;

		public Params(List<Name> names, boolean vararg) {
			this.names = Check.notNull(names);
			this.vararg = vararg;
		}

		public static Params empty() {
			return new Params(Collections.<Name>emptyList(), false);
		}

		public List<Name> names() {
			return names;
		}

		public boolean isVararg() {
			return vararg;
		}

	}

}
