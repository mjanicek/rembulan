package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.util.Check;

import java.util.HashSet;
import java.util.Set;

public abstract class Nodes {

	private Nodes() {
		// not to be instantiated
	}

	public static Target makeTarget(Sink n) {
		Check.notNull(n);

		if (n instanceof Target) {
			return (Target) n;
		}
		else {
			Target target = new Target();
			UnconditionalJump jump = new UnconditionalJump(target);

			Src prev = n.prev();
			prev.appendSink(jump);
			n.prependSource(target);

			return target;
		}
	}

	public static void inline(UnconditionalJump jmp) {
		Target tgt = jmp.target();
		if (tgt.optIncomingJump() != jmp) {
			throw new IllegalArgumentException("Jump destination cannot be inlined: " + tgt);
		}

		Src src = jmp.prev();
		Sink dest = jmp.target().next();

		jmp.target().dec(jmp);

		src.setNext(dest);
		dest.setPrev(src);
	}

	public static final Sink DUMMY_SINK = dummySink();

	private static Sink dummySink() {
		return new Sink() {

			@Override
			public void accept(NodeVisitor visitor) {
				visitor.visitNode(this);
			}

			@Override
			public Src prev() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setPrev(Src to) {
				// no-op
			}

			@Override
			public void prependSource(Src that) {
				Check.notNull(that);
				this.setPrev(that);
				that.setNext(this);
			}

			@Override
			public SlotState inSlots() {
				throw new UnsupportedOperationException();  // TODO
			}

			@Override
			public SlotState outSlots() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean pushSlots(SlotState s) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clearSlots() {
				throw new UnsupportedOperationException();  // TODO
			}

		};
	}

	public static final Src DUMMY_SRC = dummySrc();

	private static Src dummySrc() {
		return new Src() {

			@Override
			public void accept(NodeVisitor visitor) {
				// no-op
			}

			@Override
			public Sink next() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setNext(Sink to) {
				// no-op
			}

			@Override
			public void appendSink(Sink that) {
				Check.notNull(that);
				this.setNext(that);
				that.setPrev(this);
			}

			@Override
			public Src appendLinear(Linear that) {
				Check.notNull(that);
				appendSink(that);
				return that;
			}

			@Override
			public SlotState inSlots() {
				throw new UnsupportedOperationException();  // TODO
			}

			@Override
			public boolean pushSlots(SlotState s) {
				throw new UnsupportedOperationException();  // TODO
			}

			@Override
			public void clearSlots() {
				throw new UnsupportedOperationException();  // TODO
			}

			@Override
			public SlotState outSlots() {
				throw new UnsupportedOperationException();  // TODO
			}

		};
	}

	// should not modify the underlying graph!
	public static void traverseOnce(Node from, final NodeAction na) {
		final Set<Node> visited = new HashSet<>();

		NodeVisitor nv = new NodeVisitor() {
			@Override
			public boolean visitNode(Node node) {
				if (visited.contains(node)) {
					return false;
				}
				else {
					visited.add(node);
					na.visit(node);
					return true;
				}
			}

			@Override
			public void visitEdge(Node from, Node to) {
				// no-op
			}

		};

		from.accept(nv);
	}

	public static void applyTransformation(Node from, final LinearSeqTransformation tf) {
		Nodes.traverseOnce(from, new NodeAction() {
			@Override
			public void visit(Node n) {
				if (n instanceof LinearSeq) {
					LinearSeq seq = (LinearSeq) n;
					seq.apply(tf);
				}
			}
		});
	}

}
