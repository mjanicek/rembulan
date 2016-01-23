package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

import java.util.Iterator;

public class LinearSeq extends Linear {

	private final Sentinel beginSentinel;
	private final Sentinel endSentinel;

	public LinearSeq() {
		beginSentinel = new Sentinel();
		endSentinel = new Sentinel();

		beginSentinel.appendSink(endSentinel);
	}

	@Override
	public String toString() {
		Iterator<Linear> it = new LinearIterator();
		StringBuilder bld = new StringBuilder();
		bld.append("--\n");
		while (it.hasNext()) {
			Linear l = it.next();
			bld.append(l.toString()).append('\n');
		}
		bld.append("--");
		return bld.toString();
	}

	public boolean growRight() {
		Sink nxt = this.next();

		if (nxt instanceof Linear) {
			if (nxt instanceof LinearSeq) {
				throw new UnsupportedOperationException();
			}

			Linear newElem = (Linear) nxt;

			Sink newNext = newElem.next();

			this.appendSink(newNext);
			newElem.prependSource(endSentinel.prev());
			endSentinel.prependSource(newElem);

			return true;
		}
		else {
			return false;
		}
	}

	public void grow() {
		while (growRight());  // just grow as much as possible
	}

	public void apply(LinearSeqTransformation tf) {
		Check.notNull(tf);
		tf.apply(this);
	}

	public void insertAtBeginning(Linear node) {
		Sink n = beginSentinel.next();
		beginSentinel.appendSink(node);
		node.appendSink(n);
	}

	private class LinearIterator implements Iterator<Linear> {

		private Sink at;

		public LinearIterator() {
			at = beginSentinel.next();
		}

		@Override
		public boolean hasNext() {
			return (at != endSentinel);
		}

		@Override
		public Linear next() {
			Linear l = (Linear) at;
			at = l.next();
			return l;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public Iterable<Linear> nodes() {
		return new Iterable<Linear>() {
			@Override
			public Iterator<Linear> iterator() {
				return new LinearIterator();
			}
		};
	}

	public static class Sentinel extends Linear {

	}

}
