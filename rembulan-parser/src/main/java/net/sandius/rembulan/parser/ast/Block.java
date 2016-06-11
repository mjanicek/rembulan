package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class Block {

	private final List<BodyStatement> statements;
	private final ReturnStatement ret;  // may be null

	public Block(List<BodyStatement> statements, ReturnStatement ret) {
		this.statements = Check.notNull(statements);
		this.ret = ret;
	}

	public void accept(StatementVisitor visitor) {
		for (BodyStatement s : statements) {
			s.accept(visitor);
		}
		if (ret != null) {
			ret.accept(visitor);
		}
	}

}
