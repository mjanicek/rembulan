package net.sandius.rembulan.lbc.recompiler.gen.block;

import net.sandius.rembulan.util.Check;

@Deprecated
public class NodeAppender {

	private Src src;

	public NodeAppender(Src src) {
		this.src = Check.notNull(src);
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
