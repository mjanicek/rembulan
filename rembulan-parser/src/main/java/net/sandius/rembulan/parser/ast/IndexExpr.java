package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class IndexExpr extends LValueExpr {

	private final Expr object;
	private final Expr key;

	public IndexExpr(SourceInfo src, Attributes attr, Expr object, Expr key) {
		super(src, attr);
		this.object = Check.notNull(object);
		this.key = Check.notNull(key);
	}

	public Expr object() {
		return object;
	}

	public Expr key() {
		return key;
	}

	public IndexExpr update(Expr object, Expr key) {
		if (this.object.equals(object) && this.key.equals(key)) {
			return this;
		}
		else {
			return new IndexExpr(sourceInfo(), attributes(), object, key);
		}
	}

	@Override
	public LValueExpr accept(Transformer tf) {
		return tf.transform(this);
	}

}
