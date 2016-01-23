package net.sandius.rembulan.compiler.gen.block;

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
		while (it.hasNext()) {
			Linear l = it.next();
			bld.append(l.toString());
			if (it.hasNext()) bld.append('\n');
		}
		return bld.toString();
	}

	public boolean growRight() {
		if (this.next() instanceof Linear) {
			Linear newElem = (Linear) this.next();
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
