package net.sandius.rembulan.parser.ast;

/**
 * An expression that refers to a memory location, i.e. that can be assigned to.
 */
public abstract class LValueExpr extends Expr {

	protected LValueExpr(SourceInfo src) {
		super(src);
	}

}
