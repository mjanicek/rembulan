package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;
import java.util.Objects;

public class Block {

	private final List<BodyStatement> statements;
	private final ReturnStatement ret;  // may be null

	public Block(List<BodyStatement> statements, ReturnStatement ret) {
		this.statements = Check.notNull(statements);
		this.ret = ret;
	}

	public List<BodyStatement> statements() {
		return statements;
	}

	public ReturnStatement returnStatement() {
		return ret;
	}

	public Block update(List<BodyStatement> statements, ReturnStatement ret) {
		if (this.statements.equals(statements) && Objects.equals(this.ret, ret)) {
			return this;
		}
		else {
			return new Block(statements, ret);
		}
	}

}
