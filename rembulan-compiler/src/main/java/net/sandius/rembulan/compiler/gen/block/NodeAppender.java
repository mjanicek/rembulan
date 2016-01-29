package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

public class NodeAppender {

	private Src src;

	public NodeAppender(Src src) {
		Check.notNull(src);
		this.src = src;
	}

	public Src get() {
		return src;
	}

	public NodeAppender append(Linear lin) {
		src = src.appendLinear(lin);
		return this;
	}

	public void branch(Branch branch) {
		src.appendSink(branch);
		src = null;
	}

	public void term(Exit term) {
		src.appendSink(term);
		src = null;
	}

	public void jumpTo(Target tgt) {
		src.appendSink(new UnconditionalJump(tgt));
		src = null;
	}

}
