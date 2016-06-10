package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class Block {

	private final List<Statement> statements;
	private final ReturnStatement ret;  // may be null

	public Block(List<Statement> statements, ReturnStatement ret) {
		this.statements = Check.notNull(statements);
		this.ret = ret;
	}

	@Override
	public String toString() {
		return "(block [" + Util.listToString(statements, ",\n")+ "]\n" + (ret != null ? ret : "no-ret") + ")";
	}

}
