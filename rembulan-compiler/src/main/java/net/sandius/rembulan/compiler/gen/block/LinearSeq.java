package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;

import java.util.Iterator;

public class LinearSeq extends Linear implements SlotEffect {

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
//		bld.append("--\n");
		while (it.hasNext()) {
			Linear l = it.next();
			bld.append(l.toString()).append('\n');
		}
//		bld.append("--");
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

	public void dissolve() {
		Src p = this.prev();
		Sink n = this.next();

		Sink first = beginSentinel.next();
		Src last = endSentinel.prev();

		if (first == last) {
			// empty sequence
			p.appendSink(n);
		}
		else {
			// non-empty
			p.appendSink(first);
			n.prependSource(last);
		}

		beginSentinel.appendSink(endSentinel);
	}

	@Override
	public Slots effect(Slots in, Prototype prototype) {
		Slots s = in;
		for (Node n : nodes()) {
			if (n instanceof SlotEffect) {
				s = ((SlotEffect) n).effect(s, prototype);
			}
		}
		return s;
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
