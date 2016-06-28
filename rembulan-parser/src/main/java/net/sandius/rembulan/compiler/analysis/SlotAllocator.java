package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.BasicBlock;
import net.sandius.rembulan.compiler.Blocks;
import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.ir.AbstractVal;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.compiler.ir.PhiVal;
import net.sandius.rembulan.compiler.ir.UpVar;
import net.sandius.rembulan.compiler.ir.Val;
import net.sandius.rembulan.compiler.ir.Var;
import net.sandius.rembulan.compiler.ir.VarStore;
import net.sandius.rembulan.parser.util.Util;
import net.sandius.rembulan.util.Check;

import java.util.*;

public class SlotAllocator {

	private final Map<IRNode, Set<Var>> varLiveIn;
	private final Map<IRNode, Set<Var>> varLiveOut;

	private final Map<IRNode, Set<AbstractVal>> valLiveIn;

	private final Map<AbstractVal, Integer> valSlots;
	private final Map<Var, Integer> varSlots;

//	private final Map<Label, Set<Var>> endLiveVars;

	private IRNode currentNode;
	private Label currentLabel;

	private Set<Var> currentVarLiveIn;
	private Set<AbstractVal> currentValLiveIn;

	private Map<Label, Set<Var>> endVarLiveIn;
	private Map<Label, Set<AbstractVal>> endValLiveIn;

	private int paramSlots;

	public SlotAllocator() {
		this.varLiveIn = new HashMap<>();
		this.varLiveOut = new HashMap<>();

		this.valLiveIn = new HashMap<>();

		this.valSlots = new HashMap<>();
		this.varSlots = new HashMap<>();

		this.currentVarLiveIn = null;
		this.currentValLiveIn = null;

		this.endVarLiveIn = new HashMap<>();
		this.endValLiveIn = new HashMap<>();

		this.paramSlots = 0;
	}

	private static <T, U> Set<U> entries(Map<T, Set<U>> m, T k) {
		Set<U> s = m.get(k);
		if (s == null) {
			s = new HashSet<>();
			m.put(k, s);
		}
		return s;
	}

	private static <T, U> boolean update(Map<T, Set<U>> m, T k, U v, boolean present) {
		Set<U> s = entries(m, k);

		if (present) {
			return s.add(v);
		}
		else {
			return s.remove(v);
		}
	}

	private static <T, U> boolean mrg(Map<T, Set<U>> m, T k, Set<U> vs) {
		Set<U> s = entries(m, k);
		return s.addAll(vs);
	}

	private static <T, U> boolean get(Map<T, Set<U>> m, T k, U v) {
		Check.notNull(m);
		Check.notNull(k);
		Check.notNull(v);

		Set<U> s = m.get(k);
		return s != null && s.contains(v);
	}

	private boolean setLiveIn(IRNode node, AbstractVal v, boolean live) {
		System.out.println("\t" + node + " " + v + " " + live);
//		System.out.print("\t\t(val) ");
//		System.out.print("{" + Util.iterableToString(entries(valLiveIn, node), ", ") + "} -> ");

		boolean res = false;

		if (live) {
			currentValLiveIn.add(v);
		}
		else {
			currentValLiveIn.remove(v);
		}

//		res |= mrg(valLiveIn, node, currentValLiveIn);
//		res |= update(valLiveIn, node, v, live);
//		System.out.println("{" + Util.iterableToString(entries(valLiveIn, node), ", ") + "}");
		return res;
	}

	private boolean setLiveIn(IRNode node, Var v, boolean live) {
		System.out.println("\t" + node + " " + v + " " + live);
//		System.out.print("\t\t(var) ");
//		System.out.print("{" + Util.iterableToString(entries(varLiveIn, node), ", ") + "} -> ");

		boolean res = false;

		if (live) {
			currentVarLiveIn.add(v);
		}
		else {
			currentVarLiveIn.remove(v);
		}

//		res |= mrg(varLiveIn, node, currentVarLiveIn);
//		res |= update(varLiveIn, node, v, live);
//		System.out.println("{" + Util.iterableToString(entries(varLiveIn, node), ", ") + "}");
		return res;
	}

	private boolean setLiveOut(IRNode node, Var v, boolean live) {
		return update(varLiveOut, node, v, live);
	}

	private boolean isLiveIn(IRNode node, Var v) {
		return get(varLiveIn, node, v);
	}

	private boolean isLiveOut(IRNode node, Var v) {
		return get(varLiveIn, node, v);
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

	// modify s so that it contains all elements from t
	private static <T> boolean mergeInto(Set<T> s, Set<T> t) {
		boolean result = false;

		for (T e : t) {
			result |= s.add(e);
		}

		return result;
	}

//	private Set<Var> liveVars() {
//		if (currentLabel == null) {
//			throw new IllegalStateException("Querying live variables outside a block");
//		}
//		Set<Var> s = endLiveVars.get(currentLabel);
//		if (s == null) {
//			s = new HashSet<>();
//			endLiveVars.put(currentLabel, s);
//		}
//		return s;
//	}

	private class LivenessVisitor extends AbstractUseDefVisitor {

		@Override
		protected void def(Val v) {
			setLiveIn(node(), v, false);
		}

		@Override
		protected void use(Val v) {
			setLiveIn(node(), v, true);
		}

		@Override
		protected void def(PhiVal pv) {
			setLiveIn(node(), pv, false);
		}

		@Override
		protected void use(PhiVal pv) {
			setLiveIn(node(), pv, true);
		}

		@Override
		protected void def(Var v) {
			setLiveIn(node(), v, false);
		}

		@Override
		protected void use(Var v) {
			setLiveIn(node(), v, true);
		}

		@Override
		protected void def(UpVar uv) {
			// no effect on liveness
		}

		@Override
		protected void use(UpVar uv) {
			// no effect on liveness
		}

		@Override
		public void visit(VarStore node) {
			use(node.src());
			use(node.var());
		}

	}

	private BitSet occupiedSlots(IRNode node) {
		BitSet occupied = new BitSet();
		for (Var v : entries(varLiveIn, node)) {
			int idx = slotOf(v);
			if (occupied.get(idx)) {
				throw new IllegalStateException("Slot " + idx + " already occupied");
			}
			occupied.set(slotOf(v));
		}
		for (AbstractVal v : entries(valLiveIn, node)) {
			int idx = slotOf(v);
			if (occupied.get(idx)) {
				throw new IllegalStateException("Slot " + idx + " already occupied");
			}
			occupied.set(slotOf(v));
		}
		return occupied;
	}

	private int findFreeSlot(IRNode node) {
		BitSet occupied = occupiedSlots(node);

		int idx = 0;
		while (occupied.get(idx)) {
			idx++;
		}

		assert (!occupied.get(idx));

		return idx;
	}

	private void assignSlot(Var v) {
		varSlots.put(v, paramSlots++);
	}

	private void assignSlot(Var v, IRNode node) {
		if (hasSlot(v)) {
			throw new IllegalStateException("Slot already assigned for variable " + v);
		}
		varSlots.put(v, findFreeSlot(node));
	}

	private void assignSlot(AbstractVal v, IRNode node) {
		if (hasSlot(v)) {
			throw new IllegalStateException("Slot already assigned for value " + v);
		}
		valSlots.put(v, findFreeSlot(node));
	}

	private class AllocatorVisitor extends AbstractUseDefVisitor {

		@Override
		protected void def(Val v) {
			if (hasSlot(v)) {
				throw new IllegalStateException("Value " + v + " already assigned to a slot");
			}
			assignSlot(v, node());
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
				assignSlot(pv, node());
			}
		}

		@Override
		protected void use(PhiVal pv) {
			if (!hasSlot(pv)) {
				throw new IllegalStateException("Value " + pv + " not assigned to a slot");
			}
		}

		@Override
		protected void def(Var v) {
			if (!hasSlot(v)) {
				assignSlot(v, node());
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

	public SlotAllocInfo result() {
		return new SlotAllocInfo(
				Collections.unmodifiableMap(valSlots),
				Collections.unmodifiableMap(varSlots));
	}

	private Iterable<Label> livenessCheckOrder(Blocks blocks) {
		Check.notNull(blocks);

		Map<Label, BasicBlock> index = blocks.index();

		ArrayList<Label> result = new ArrayList<>();
		Set<Label> visited = new HashSet<>();
		Queue<Label> open = new ArrayDeque<>();

		open.add(blocks.entryLabel());

		while (!open.isEmpty()) {
			Label l = open.poll();
			BasicBlock bb = index.get(l);
			if (visited.add(l)) {
				result.add(l);
				for (Label nxt : bb.end().nextLabels()) {
					open.add(nxt);
				}
			}
		}

		result.trimToSize();
//		Collections.reverse(result);

		return result;
	}

	private Map<Label, Set<Label>> inLabels(Blocks blocks) {
		Check.notNull(blocks);

		Map<Label, BasicBlock> index = blocks.index();

		Map<Label, Set<Label>> result = new HashMap<>();

		Set<Label> visited = new HashSet<>();
		Stack<Label> open = new Stack<>();

		open.add(blocks.entryLabel());

		while (!open.isEmpty()) {
			Label l = open.pop();
			BasicBlock bb = index.get(l);

			update(result, l, l, false);  // make sure l is initialised

			for (Label nxt : bb.end().nextLabels()) {
				update(result, nxt, l, true);
			}

			if (visited.add(l)) {
				for (Label nxt : bb.end().nextLabels()) {
					open.add(nxt);
				}
			}
		}

		return Collections.unmodifiableMap(result);
	}

	public SlotAllocInfo process(IRFunc fn) {
		Blocks blocks = fn.blocks();

		Map<Label, BasicBlock> index = blocks.index();
		Map<Label, Set<Label>> in = inLabels(blocks);

		for (Label l : index.keySet()) {
			endVarLiveIn.put(l, new HashSet<Var>());
			endValLiveIn.put(l, new HashSet<AbstractVal>());
		}

		Iterable<Label> order = livenessCheckOrder(blocks);

		System.out.println("Traversal order: " + Util.iterableToString(order, ", "));
		System.out.println();

		boolean changed;

		Stack<Label> open = new Stack<>();
		for (Label l : order) {
			open.push(l);
		}

		while (!open.isEmpty()) {
			Label l = open.pop();
			System.out.println("Processing label " + l);

			currentVarLiveIn = new HashSet<>(endVarLiveIn.get(l));
			currentValLiveIn = new HashSet<>(endValLiveIn.get(l));

			boolean chg = doBlockLiveness(index.get(l));

			for (Label inl : in.get(l)) {
				boolean nextChg;

				System.out.println("Pushing into label " + inl);

				System.out.print("\t\t(var) {" + Util.iterableToString(endVarLiveIn.get(inl), ", ") + "}");
				System.out.print(" -> ");

				nextChg = endVarLiveIn.get(inl).addAll(currentVarLiveIn);

				System.out.println("{" + Util.iterableToString(endVarLiveIn.get(inl), ", ") + "}");


				System.out.print("\t\t(val) {" + Util.iterableToString(endValLiveIn.get(inl), ", ") + "}");
				System.out.print(" -> ");

				nextChg |= endValLiveIn.get(inl).addAll(currentValLiveIn);

				System.out.println("{" + Util.iterableToString(endValLiveIn.get(inl), ", ") + "}");

//				if (nextChg && !open.contains(inl)) {
				if (nextChg) {
					System.out.println("\t\t(change!)");
					if (open.contains(inl)) {
						open.remove(inl);
					}
					open.push(inl);
				}
			}
		}

/*
		do {
			Iterator<Label> it = order.iterator();
			changed = false;

			while (it.hasNext()) {
//			while (it.hasNext() && !changed) {
				Label l = it.next();
				System.out.println("At label " + l);
				BasicBlock b = index.get(l);

				assert (b != null);

				currentVarLiveIn = new HashSet<>(endVarLiveIn.get(l));
				currentValLiveIn = new HashSet<>(endValLiveIn.get(l));

				changed = doBlock(b);

				for (Label inl : in.get(l)) {
					System.out.println("Pushing into label " + inl);

					System.out.print("\t\t(var) {" + Util.iterableToString(endVarLiveIn.get(inl), ", ") + "}");
					System.out.print(" -> ");

					changed |= endVarLiveIn.get(inl).addAll(currentVarLiveIn);

					System.out.println("{" + Util.iterableToString(endVarLiveIn.get(inl), ", ") + "}");


					System.out.print("\t\t(val) {" + Util.iterableToString(endValLiveIn.get(inl), ", ") + "}");
					System.out.print(" -> ");

					changed |= endValLiveIn.get(inl).addAll(currentValLiveIn);

					System.out.println("{" + Util.iterableToString(endValLiveIn.get(inl), ", ") + "}");
				}

				currentVarLiveIn = null;
				currentValLiveIn = null;
			}

			if (changed) {
				System.out.println("Changes detected, will loop again");
			}

		} while (changed);
*/

		System.out.println("Liveness at block ends");
		{
			Iterator<BasicBlock> it = blocks.blockIterator();
			while (it.hasNext()) {
				BasicBlock b = it.next();
				Label l = b.label();
				System.out.println("\t" + l + " ... vars = {"
						+ Util.iterableToString(endVarLiveIn.get(l), ", ") + "}; vals = {"
						+ Util.iterableToString(endValLiveIn.get(l), ", ") + "}");
			}
		}

		System.out.println("Live vars:");
		for (Map.Entry<IRNode, Set<Var>> e : varLiveIn.entrySet()) {
			System.out.println("\t" + e.getKey() + " -> {" + Util.iterableToString(e.getValue(), ", ") + "}");
		}
		System.out.println("Live vals:");
		for (Map.Entry<IRNode, Set<AbstractVal>> e : valLiveIn.entrySet()) {
			System.out.println("\t" + e.getKey() + " -> {" + Util.iterableToString(e.getValue(), ", ") + "}");
		}

		// now slot allocation
		{
			Set<Label> visited = new HashSet<>();
			open.push(blocks.entryLabel());

			AllocatorVisitor visitor = new AllocatorVisitor();

			// assign slots to parameters
			for (Var p : fn.params()) {
				assignSlot(p);
			}

			while (!open.isEmpty()) {
				Label l = open.pop();
				if (visited.add(l)) {
					BasicBlock b = index.get(l);

					assignSlots(b, visitor);

					for (Label n : b.end().nextLabels()) {
						open.push(n);
					}
				}
			}
		}

		System.out.println("Variable slots");
		for (Var v : varSlots.keySet()) {
			System.out.println("\t" + v + " -> " + slotOf(v));
		}
		System.out.println("Value slots");
		for (AbstractVal v : valSlots.keySet()) {
			System.out.println("\t" + v + " -> " + slotOf(v));
		}

		return result();

//		Stack<Label> open = new Stack<>();
//
//		while (!open.isEmpty()) {
//			Label l = open.pop();
//			BasicBlock b = index.get(l);
//
//			assert (b != null);
//
//			changed = false;
//			try {
//				visit(b);
//				if (changed) {
//					for (Label nxt : b.end().nextLabels()) {
//						open.add(nxt);
//					}
//				}
//			}
//			finally {
//				changed = false;
//			}
//		}

	}

	private void assignSlots(BasicBlock b, AllocatorVisitor visitor) {
		System.out.println("Assigning slots to " + b.label());
		for (BodyNode n : b.body()) {
			currentNode = n;  // FIXME
			n.accept(visitor);
			currentNode = null;
		}
		currentNode = b.end();  // FIXME
		b.end().accept(visitor);
		currentNode = null;
	}

	private boolean doNode(LivenessVisitor visitor, IRNode node) {
		Check.notNull(node);
		System.out.println("In node " + node);

		final boolean changed;

		try {
			currentNode = node;

			Set<AbstractVal> valLive = entries(valLiveIn, node);
			Set<Var> varLive = entries(varLiveIn, node);

			node.accept(visitor);

			boolean varSame = currentVarLiveIn.equals(varLive);
			boolean valSame = currentValLiveIn.equals(valLive);

			if (!varSame) {
				System.out.println("\t\t(var) {" + Util.iterableToString(varLive, ", ") + "} -> {" + Util.iterableToString(currentVarLiveIn, ", ") + "}");
				varLive.clear();
				varLive.addAll(currentVarLiveIn);
			}

			if (!valSame) {
				System.out.println("\t\t(val) {" + Util.iterableToString(valLive, ", ") + "} -> {" + Util.iterableToString(currentValLiveIn, ", ") + "}");
				valLive.clear();
				valLive.addAll(currentValLiveIn);
			}

			changed = !varSame || !valSame;
		}
		finally {
			currentNode = null;
		}

		return changed;
	}

	private boolean doBlockLiveness(BasicBlock block) {
		LivenessVisitor udv = new LivenessVisitor();

		boolean changed = false;

		// iterating backwards
		changed |= doNode(udv, block.end());
		for (int i = block.body().size() - 1; i >= 0; i--) {
			changed |= doNode(udv, block.body().get(i));
		}

		return changed;
	}

}
