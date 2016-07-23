package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.util.Check;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

public class SlotAllocator {

	private final IRFunc fn;

	private final Map<AbstractVal, Integer> valSlots;
	private final Map<Var, Integer> varSlots;

	private IRNode currentNode;

	public SlotAllocator(IRFunc fn) {
		this.fn = Check.notNull(fn);
		this.valSlots = new HashMap<>();
		this.varSlots = new HashMap<>();
	}

	public static SlotAllocInfo allocateSlots(IRFunc fn) {
		SlotAllocator allocator = new SlotAllocator(fn);
		return allocator.process();
	}

	private IRNode node() {
		if (currentNode == null) {
			throw new IllegalStateException("Current node is null");
		}
		return currentNode;
	}

	private boolean hasSlot(Var v) {
		return varSlots.get(Check.notNull(v)) != null;
	}

	private boolean hasSlot(AbstractVal v) {
		return valSlots.get(Check.notNull(v)) != null;
	}

	private int slotOf(Var v) {
		Check.notNull(v);
		Integer idx = varSlots.get(v);
		if (idx == null) {
			throw new NoSuchElementException("Slot not defined for variable " + v);
		}
		else {
			return idx;
		}
	}

	private int slotOf(AbstractVal v) {
		Check.notNull(v);
		Integer idx = valSlots.get(v);
		if (idx == null) {
			throw new NoSuchElementException("Slot not defined for value " + v);
		}
		else {
			return idx;
		}
	}

	private BitSet occupiedSlots(LivenessInfo liveness, IRNode node) {
		BitSet occupied = new BitSet();

		LivenessInfo.Entry e = liveness.entry(node);

		for (Var v : e.inVar()) {
			int idx = slotOf(v);
			if (occupied.get(idx)) {
				throw new IllegalStateException("Slot " + idx + " already occupied");
			}
			if (e.outVar().contains(v)) {
				occupied.set(slotOf(v));
			}
		}
		for (AbstractVal v : e.inVal()) {
			int idx = slotOf(v);
			if (occupied.get(idx)) {
				throw new IllegalStateException("Slot " + idx + " already occupied");
			}
			if (e.outVal().contains(v)) {
				occupied.set(slotOf(v));
			}
		}
		return occupied;
	}

	private int findFreeSlot(LivenessInfo liveness, IRNode node) {
		BitSet occupied = occupiedSlots(liveness, node);

		int idx = 0;
		while (occupied.get(idx)) {
			idx++;
		}

		assert (!occupied.get(idx));

		return idx;
	}

	private void assignParamSlots(List<Var> params) {
		int idx = 0;
		for (Var v : params) {
			varSlots.put(v, idx++);
		}
	}

	private void assignSlot(Var v, LivenessInfo liveness, IRNode node) {
		if (hasSlot(v)) {
			throw new IllegalStateException("Slot already assigned for variable " + v);
		}
		varSlots.put(v, findFreeSlot(liveness, node));
	}

	private void assignSlot(AbstractVal v, LivenessInfo liveness, IRNode node) {
		if (hasSlot(v)) {
			throw new IllegalStateException("Slot already assigned for value " + v);
		}
		valSlots.put(v, findFreeSlot(liveness, node));
	}

	public SlotAllocInfo process() {
		LivenessInfo liveness = LivenessAnalyser.computeLiveness(fn);

		Set<Label> visited = new HashSet<>();
		Stack<Label> open = new Stack<>();
		open.push(fn.code().entryLabel());

		AllocatorVisitor visitor = new AllocatorVisitor(liveness);

		assignParamSlots(fn.params());

		while (!open.isEmpty()) {
			Label l = open.pop();
			if (visited.add(l)) {
				BasicBlock b = fn.code().block(l);

				assignSlots(b, visitor);

				for (Label n : b.end().nextLabels()) {
					open.push(n);
				}
			}
		}

		return new SlotAllocInfo(
				Collections.unmodifiableMap(valSlots),
				Collections.unmodifiableMap(varSlots));
	}

	private void assignSlots(BasicBlock b, AllocatorVisitor visitor) {
		for (BodyNode n : b.body()) {
			currentNode = n;  // FIXME
			n.accept(visitor);
			currentNode = null;
		}
		currentNode = b.end();  // FIXME
		b.end().accept(visitor);
		currentNode = null;
	}

	private class AllocatorVisitor extends AbstractUseDefVisitor {

		private final LivenessInfo liveness;

		AllocatorVisitor(LivenessInfo liveness) {
			this.liveness = Check.notNull(liveness);
		}

		@Override
		protected void def(Val v) {
			if (hasSlot(v)) {
				throw new IllegalStateException("Value " + v + " already assigned to a slot");
			}
			assignSlot(v, liveness, node());
		}

		@Override
		protected void use(Val v) {
			if (!hasSlot(v)) {
				throw new IllegalStateException("Value " + v + " not assigned to a slot");
			}
		}

		@Override
		protected void def(PhiVal pv) {
			if (hasSlot(pv)) {
				// ok: from another branch
			}
			else {
				assignSlot(pv, liveness, node());
			}
		}

		@Override
		protected void use(PhiVal pv) {
			if (!hasSlot(pv)) {
				throw new IllegalStateException("Value " + pv + " not assigned to a slot");
			}
		}

		@Override
		protected void def(MultiVal mv) {
			// TODO
		}

		@Override
		protected void use(MultiVal mv) {
			// TODO
		}

		@Override
		protected void def(Var v) {
			if (!hasSlot(v)) {
				assignSlot(v, liveness, node());
			}
			else {
				// ok: just use it
			}
		}

		@Override
		protected void use(Var v) {
			if (!hasSlot(v)) {
				throw new IllegalStateException("No slot assigned to variable " + v);
			}
		}

		@Override
		protected void def(UpVar uv) {
			// no effect on slots
		}

		@Override
		protected void use(UpVar uv) {
			// no effect on slots
		}

	}

}
