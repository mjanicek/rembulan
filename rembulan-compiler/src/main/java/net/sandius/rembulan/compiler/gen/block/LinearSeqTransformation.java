package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.List;

public abstract class LinearSeqTransformation {

	public abstract void apply(LinearSeq seq);

	public static class Remove extends LinearSeqTransformation {

		public final LinearPredicate predicate;

		public Remove(LinearPredicate predicate) {
			Check.notNull(predicate);
			this.predicate = predicate;
		}

		@Override
		public void apply(LinearSeq seq) {
			List<Linear> nodes = new ArrayList<>();
			for (Linear n : seq.nodes()) {
				if (predicate.apply(n)) nodes.add(n);
			}

			for (Linear n : nodes) {
				n.remove();
			}
		}

	}

}
