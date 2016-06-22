package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.BlockTermNode;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.compiler.ir.ToNext;
import net.sandius.rembulan.util.Check;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class BasicBlock {

	private final Label label;
	private final List<IRNode> body;
	private final BlockTermNode end;

	public BasicBlock(Label label, List<IRNode> body, BlockTermNode end) {
		this.label = Check.notNull(label);
		this.body = Check.notNull(body);
		this.end = Check.notNull(end);
	}

	public Label label() {
		return label;
	}

	public List<IRNode> body() {
		return body;
	}

	public BlockTermNode end() {
		return end;
	}

	public Iterator<IRNode> iterator() {
		return new Iterator<IRNode>() {

			private int idx = -1;

			@Override
			public boolean hasNext() {
				return idx < body.size() + 1;
			}

			@Override
			public IRNode next() {
				if (idx < body.size() + 1) {
					if (idx < 0) {
						idx += 1;
						return new ToNext(label);  // FIXME
//						return label;
					}
					else if (idx >= body.size()) {
						idx += 1;
						return end;
					}
					else {
						return body.get(idx++);
					}
				}
				else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

}
