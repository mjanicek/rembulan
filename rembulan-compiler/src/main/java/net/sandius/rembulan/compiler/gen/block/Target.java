package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

import java.util.HashMap;
import java.util.Map;

public class Target implements Node, Src {

	private final Map<Jump, Integer> in;
	private Sink next;

	public Target() {
		this.in = new HashMap<>();
		this.next = Nodes.DUMMY_SINK;
	}

	public void inc(Jump jmp) {
		Check.notNull(jmp);
		in.put(jmp, (in.containsKey(jmp) ? in.get(jmp) : 0) + 1);
	}

	public void dec(Jump jmp) {
		Check.notNull(jmp);
		int count = in.containsKey(jmp) ? in.get(jmp) : 0;
		if (count > 1) {
			in.put(jmp, count - 1);
		}
		else {
			in.remove(jmp);
		}
	}

	@Override
	public Sink next() {
		return next;
	}

	@Override
	public void setNext(Sink to) {
		Check.notNull(to);
		this.next = to;
	}

	@Override
	public void appendSink(Sink that) {
		Check.notNull(that);
		this.setNext(that);
		that.setPrev(this);
	}

	@Override
	public void accept(NodeVisitor visitor) {
		if (visitor.visit(this)) {
			next.accept(visitor);
		}
	}

	// if there is a single unconditional jump to this target.
	// return it, otherwise return null.
	public UnconditionalJump isSingleTarget() {
		UnconditionalJump jmp = null;

		for (Jump origin : in.keySet()) {
			int count = in.get(origin);
			if (count > 1) {
				return null;
			}

			if (jmp != null) {
				// we already have a candidate!
				return null;
			}

			if (origin instanceof UnconditionalJump) {
				jmp = (UnconditionalJump) origin;
			}
			else {
				// not an unconditional jump!
				return null;
			}
		}
		return null;
	}

}
