package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

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

		};
	}

}
