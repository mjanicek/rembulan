package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class LabelStatement extends BodyStatement {

	private final Name labelName;

	public LabelStatement(Attributes attr, Name labelName) {
		super(attr);
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
			return new LabelStatement(attributes(), labelName);
		}
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
