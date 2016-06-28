package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.BasicBlock;
import net.sandius.rembulan.compiler.Blocks;
import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.compiler.util.BlockUtils;
import net.sandius.rembulan.parser.util.Util;
import net.sandius.rembulan.util.Check;

import java.util.*;

public class LivenessAnalyser {

	private final IRFunc fn;

	private final Map<IRNode, Set<Var>> varLiveIn;
	private final Map<IRNode, Set<AbstractVal>> valLiveIn;

	private Map<Label, Set<Var>> endVarLiveIn;
	private Map<Label, Set<AbstractVal>> endValLiveIn;

	private LivenessAnalyser(IRFunc fn) {
		this.fn = Check.notNull(fn);

		this.varLiveIn = new HashMap<>();
		this.valLiveIn = new HashMap<>();

		this.endVarLiveIn = new HashMap<>();
		this.endValLiveIn = new HashMap<>();
	}

	public static LivenessInfo computeLiveness(IRFunc fn) {
		LivenessAnalyser analyser = new LivenessAnalyser(fn);
		return analyser.analyse();
	}

	public LivenessInfo analyse() {
		Blocks blocks = fn.blocks();

		Map<Label, BasicBlock> index = blocks.index();
		Map<Label, Set<Label>> in = BlockUtils.inLabels(index, blocks.entryLabel());

		// initialise
		{
			for (Label l : index.keySet()) {
				endVarLiveIn.put(l, new HashSet<Var>());
				endValLiveIn.put(l, new HashSet<AbstractVal>());
			}

			Iterator<IRNode> ns = BlockUtils.nodeIterator(blocks);
			while (ns.hasNext()) {
				IRNode n = ns.next();
				varLiveIn.put(n, new HashSet<Var>());
				valLiveIn.put(n, new HashSet<AbstractVal>());
			}
		}

		Stack<Label> open = new Stack<>();

		// make sure we'll visit all labels at least once
		for (Label l : BlockUtils.labelsBreadthFirst(index, blocks.entryLabel())) {
			open.push(l);
		}

		while (!open.isEmpty()) {
			Label l = open.pop();

			LivenessVisitor visitor = new LivenessVisitor(
					endVarLiveIn.get(l),
					endValLiveIn.get(l));

			processBlock(visitor, index.get(l));

			for (Label inl : in.get(l)) {
				boolean changed = false;

				changed |= endVarLiveIn.get(inl).addAll(visitor.currentVarLiveIn());
				changed |= endValLiveIn.get(inl).addAll(visitor.currentValLiveIn());

				if (changed) {
					if (open.contains(inl)) {
						open.remove(inl);
					}
					open.push(inl);
				}
			}

		}

		return result();
	}

	private static void mergeLiveOut(Map<IRNode, LivenessInfo.Entry> entries, IRNode m, IRNode n) {
		LivenessInfo.Entry e_m = entries.get(m);
		LivenessInfo.Entry e_n = entries.get(n);
		e_m.outVar().addAll(e_n.inVar());
		e_m.outVal().addAll(e_n.inVal());
	}

	private LivenessInfo result() {
		Map<Label, BasicBlock> index = fn.blocks().index();

		Map<IRNode, Set<Var>> varLiveOut = new HashMap<>();
		Map<IRNode, Set<AbstractVal>> valLiveOut = new HashMap<>();

		Map<IRNode, LivenessInfo.Entry> entries = new HashMap<>();

		// initialise
		Iterator<IRNode> nodeIterator = BlockUtils.nodeIterator(fn.blocks());
		while (nodeIterator.hasNext()) {
			IRNode node = nodeIterator.next();

			Set<Var> var_in = varLiveIn.get(node);
			Set<AbstractVal> val_in = valLiveIn.get(node);

			entries.put(node, new LivenessInfo.Entry(var_in, new HashSet<Var>(), val_in, new HashSet<AbstractVal>()));
		}

		// compute live-out from live-in
		Iterator<BasicBlock> blockIterator = fn.blocks().blockIterator();
		while (blockIterator.hasNext()) {
			BasicBlock b = blockIterator.next();

			// body
			for (int i = 0; i < b.body().size(); i++) {
				BodyNode m = b.body().get(i);
				IRNode n = i + 1 < b.body().size() ? b.body().get(i + 1) : b.end();
				mergeLiveOut(entries, m, n);
			}

			// end
			BlockTermNode end = b.end();
			LivenessInfo.Entry e_end = entries.get(end);

			for (Label nxt : end.nextLabels()) {
				BasicBlock nextBlock = index.get(nxt);
				IRNode n = !nextBlock.body().isEmpty() ? nextBlock.body().get(0) : nextBlock.end();
				mergeLiveOut(entries, end, n);
			}

		}

		return new LivenessInfo(entries);
	}

	private boolean processBlock(LivenessVisitor visitor, BasicBlock block) {
		// iterating backwards
		boolean changed = processNode(visitor, block.end());

		for (int i = block.body().size() - 1; i >= 0; i--) {
			changed |= processNode(visitor, block.body().get(i));
		}

		return changed;

	}

	private boolean processNode(LivenessVisitor visitor, IRNode node) {
		Check.notNull(node);

		final Set<Var> varLive_in = varLiveIn.get(node);
		final Set<AbstractVal> valLive_in = valLiveIn.get(node);

		node.accept(visitor);

		boolean varSame = visitor.currentVarLiveIn().equals(varLive_in);
		boolean valSame = visitor.currentValLiveIn().equals(valLive_in);

		if (!varSame) {
			varLive_in.clear();
			varLive_in.addAll(visitor.currentVarLiveIn());
		}

		if (!valSame) {
			valLive_in.clear();
			valLive_in.addAll(visitor.currentValLiveIn());
		}

		return !varSame || !valSame;
	}

	private class LivenessVisitor extends AbstractUseDefVisitor {

		private Set<Var> currentVarLiveIn;
		private Set<AbstractVal> currentValLiveIn;

		public LivenessVisitor(Set<Var> currentVarLiveIn, Set<AbstractVal> currentValLiveIn) {
			this.currentVarLiveIn = new HashSet<>(Check.notNull(currentVarLiveIn));
			this.currentValLiveIn = new HashSet<>(Check.notNull(currentValLiveIn));
		}

		public Set<Var> currentVarLiveIn() {
			return currentVarLiveIn;
		}

		public Set<AbstractVal> currentValLiveIn() {
			return currentValLiveIn;
		}

		@Override
		protected void def(Val v) {
			currentValLiveIn.remove(v);
		}

		@Override
		protected void use(Val v) {
			currentValLiveIn.add(v);
		}

		@Override
		protected void def(PhiVal pv) {
			currentValLiveIn.remove(pv);
		}

		@Override
		protected void use(PhiVal pv) {
			currentValLiveIn.add(pv);
		}

		@Override
		protected void def(Var v) {
			currentVarLiveIn.remove(v);
		}

		@Override
		protected void use(Var v) {
			currentVarLiveIn.add(v);
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
			use(node.var());  // Note that this is a use, not a def
		}

	}

}
