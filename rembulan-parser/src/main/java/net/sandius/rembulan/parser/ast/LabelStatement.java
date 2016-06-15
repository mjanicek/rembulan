package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class LabelStatement extends BodyStatement {

	private final Name labelName;

	public LabelStatement(SourceInfo src, Name labelName) {
		super(src);
		this.labelName = Check.notNull(labelName);
	}

	public Name labelName() {
		return labelName;
	}

	public LabelStatement update(Name labelName) {
		if (this.labelName.equals(labelName)) {
			return this;
		}
		else {
			return new LabelStatement(sourceInfo(), labelName);
		}
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public BodyStatement acceptTransformer(Transformer tf) {
		return tf.transform(this);
	}

}
