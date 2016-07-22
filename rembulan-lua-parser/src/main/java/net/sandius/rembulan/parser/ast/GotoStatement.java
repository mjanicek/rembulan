package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class GotoStatement extends BodyStatement {

	private final Name labelName;

	public GotoStatement(Attributes attr, Name labelName) {
		super(attr);
		this.labelName = Check.notNull(labelName);
	}

	public Name labelName() {
		return labelName;
	}

	public GotoStatement update(Name labelName) {
		if (this.labelName.equals(labelName)) {
			return this;
		}
		else {
			return new GotoStatement(attributes(), labelName);
		}
	}

	public GotoStatement withAttributes(Attributes attr) {
		if (attributes().equals(attr)) return this;
		else return new GotoStatement(attr, labelName);
	}

	public GotoStatement with(Object o) {
		return this.withAttributes(attributes().with(o));
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
