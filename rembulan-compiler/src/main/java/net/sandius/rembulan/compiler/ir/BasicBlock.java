package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.List;
import java.util.Objects;

public class BasicBlock {

	private final Label label;
	private final List<BodyNode> body;
	private final BlockTermNode end;

	public BasicBlock(Label label, List<BodyNode> body, BlockTermNode end) {
		this.label = Check.notNull(label);
		this.body = Check.notNull(body);
		this.end = Check.notNull(end);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BasicBlock that = (BasicBlock) o;
		return this.label.equals(that.label) &&
				this.body.equals(that.body) &&
				this.end.equals(that.end);
	}

	@Override
	public int hashCode() {
		return Objects.hash(label, body, end);
	}

	public Label label() {
		return label;
	}

	public List<BodyNode> body() {
		return body;
	}

	public BlockTermNode end() {
		return end;
	}

}
