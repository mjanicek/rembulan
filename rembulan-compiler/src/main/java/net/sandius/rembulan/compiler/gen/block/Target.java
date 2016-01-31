package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.util.Check;

import java.util.HashMap;
import java.util.Map;

public class Target implements Node, Src {

	private Slots inSlots;

	private final String name;
	private final Map<Jump, Integer> in;
	private Sink next;

	public Target(String name) {
		this.inSlots = null;
		this.name = name;
		this.in = new HashMap<>();
		this.next = Nodes.DUMMY_SINK;
	}

	public Target() {
		this(null);
	}

	@Override
	public String toString() {
		return "@" + (name != null ? name : Integer.toHexString(System.identityHashCode(this)));
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
	public Src appendLinear(Linear that) {
		Check.notNull(that);
		appendSink(that);
		return that;
	}

	@Override
	public void accept(NodeVisitor visitor) {
		if (visitor.visitNode(this)) {
			visitor.visitEdge(this, next);
			next.accept(visitor);
		}
	}

	// if there is a single unconditional jump to this target.
	// return it, otherwise return null.
	public UnconditionalJump optIncomingJump() {
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
		return jmp;
	}

	@Override
	public Slots inSlots() {
		return inSlots;
	}

	@Override
	public Slots outSlots() {
		return effect(inSlots);
	}

	@Override
	public Slots effect(Slots in) {
		return in;
	}

	@Override
	public boolean pushSlots(Slots in) {
		Check.notNull(in);
		Slots o = inSlots;
		Slots n = inSlots == null ? in : inSlots.join(in);
		if (!n.equals(o)) {
			inSlots = n;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void clearSlots() {
		inSlots = null;
	}

}
