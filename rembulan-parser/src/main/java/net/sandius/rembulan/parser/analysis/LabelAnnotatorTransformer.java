package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.parser.ast.BodyStatement;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.FunctionDefExpr;
import net.sandius.rembulan.parser.ast.GotoStatement;
import net.sandius.rembulan.parser.ast.LabelStatement;
import net.sandius.rembulan.parser.ast.Transformer;

public abstract class LabelAnnotatorTransformer extends Transformer {

	protected abstract Object annotation(LabelStatement node);

	protected abstract Object annotation(GotoStatement node);

	@Override
	public BodyStatement transform(LabelStatement node) {
		return node.with(annotation(node));
	}

	@Override
	public BodyStatement transform(GotoStatement node) {
		return node.with(annotation(node));
	}

	@Override
	public Expr transform(FunctionDefExpr e) {
		// don't descend into function literals
		return e;
	}

}
