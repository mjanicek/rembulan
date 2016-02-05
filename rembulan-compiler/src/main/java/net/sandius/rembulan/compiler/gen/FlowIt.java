package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.AccountingNode;
import net.sandius.rembulan.compiler.gen.block.Branch;
import net.sandius.rembulan.compiler.gen.block.Capture;
import net.sandius.rembulan.compiler.gen.block.Entry;
import net.sandius.rembulan.compiler.gen.block.HookNode;
import net.sandius.rembulan.compiler.gen.block.LineInfo;
import net.sandius.rembulan.compiler.gen.block.Linear;
import net.sandius.rembulan.compiler.gen.block.LinearSeq;
import net.sandius.rembulan.compiler.gen.block.LinearSeqTransformation;
import net.sandius.rembulan.compiler.gen.block.LocalVariableEffect;
import net.sandius.rembulan.compiler.gen.block.Node;
import net.sandius.rembulan.compiler.gen.block.NodeAppender;
import net.sandius.rembulan.compiler.gen.block.NodeVisitor;
import net.sandius.rembulan.compiler.gen.block.Nodes;
import net.sandius.rembulan.compiler.gen.block.ResumptionPoint;
import net.sandius.rembulan.compiler.gen.block.Sink;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.compiler.gen.block.UnconditionalJump;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntBuffer;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class FlowIt {

	public final Prototype prototype;

	public Entry callEntry;
	public Set<ResumptionPoint> resumePoints;

	public Map<Node, Edges> reachabilityGraph;

	public FlowIt(Prototype prototype) {
		this.prototype = prototype;
	}

	public void go() {
		IntVector code = prototype.getCode();
		Target[] targets = new Target[code.length()];
		for (int pc = 0; pc < targets.length; pc++) {
			targets[pc] = new Target(Integer.toString(pc + 1));
		}

		ReadOnlyArray<Target> pcLabels = ReadOnlyArray.wrap(targets);

		LuaInstructionToNodeTranslator translator = new LuaInstructionToNodeTranslator(prototype, pcLabels);

		for (int pc = 0; pc < pcLabels.size(); pc++) {
			translator.translate(pc);
		}

//		System.out.println("[");
//		for (int i = 0; i < pcLabels.size(); i++) {
//			NLabel label = pcLabels.get(i);
//			System.out.println(i + ": " + label.toString());
//		}
//		System.out.println("]");

		callEntry = new Entry("main", ArgTypes.init(prototype.getNumberOfParameters(), prototype.isVararg()), prototype.getMaximumStackSize(), pcLabels.get(0));

		resumePoints = new HashSet<>();

		insertHooks();

		inlineInnerJumps();
		makeBlocks();

		applyTransformation(new CollectCPUAccounting());

		// remove repeated line info nodes
		applyTransformation(new RemoveRedundantLineNodes());

		// dissolve blocks
		dissolveBlocks();

		// remove all line info nodes
//		applyTransformation(entryPoints, new LinearSeqTransformation.Remove(Predicates.isClass(LineInfo.class)));

//		System.out.println();
//		printNodes(entryPoints);

		updateReachability();
		updateDataFlow();

		inlineBranches();

		// add capture nodes
		insertCaptureNodes();

//		addResumptionPoints();

		makeBlocks();

		updateReachability();
		updateDataFlow();

	}

	public void insertHooks() {
		// the call hook
		Target oldEntryTarget = callEntry.target();
		Target newEntryTarget = new Target();
		NodeAppender appender = new NodeAppender(newEntryTarget);
		appender
				.append(new HookNode.Call())
				.jumpTo(oldEntryTarget);

		callEntry.setTarget(newEntryTarget);

		// TODO: return hooks
	}

	private static class CollectCPUAccounting extends LinearSeqTransformation {

		@Override
		public void apply(LinearSeq seq) {
			List<AccountingNode> toBeRemoved = new ArrayList<>();

			int cost = 0;

			for (Linear n : seq.nodes()) {
				if (n instanceof AccountingNode) {
					AccountingNode an = (AccountingNode) n;
					if (n instanceof AccountingNode.TickBefore) {
						cost += 1;
						toBeRemoved.add(an);
					}
					else if (n instanceof AccountingNode.Add) {
						cost += ((AccountingNode.Add) n).cost;
						toBeRemoved.add(an);
					}
				}
			}

			for (AccountingNode an : toBeRemoved) {
				// remove all nodes
				an.remove();
			}

			if (cost > 0) {
				// insert cost node at the beginning
				seq.insertAtBeginning(new AccountingNode.Add(cost));
			}
		}

	}

	private static class RemoveRedundantLineNodes extends LinearSeqTransformation {

		@Override
		public void apply(LinearSeq seq) {
			int line = -1;
			List<Linear> toBeRemoved = new ArrayList<>();

			for (Linear n : seq.nodes()) {
				if (n instanceof LineInfo) {
					LineInfo lineInfoNode = (LineInfo) n;
					if (lineInfoNode.line == line) {
						// no need to keep this one
						toBeRemoved.add(lineInfoNode);
					}
					line = lineInfoNode.line;
				}
			}

			for (Linear n : toBeRemoved) {
				n.remove();
			}

		}

	}

	private void applyTransformation(LinearSeqTransformation tf) {
		for (Node n : reachableNodes(Collections.singleton(callEntry))) {
			if (n instanceof LinearSeq) {
				LinearSeq seq = (LinearSeq) n;
				seq.apply(tf);
			}
		}
	}

	private void inlineInnerJumps() {
		for (Node n : reachableNodes(Collections.singleton(callEntry))) {
			if (n instanceof UnconditionalJump) {
				((UnconditionalJump) n).tryInlining();
			}
		}
	}

	private void inlineBranches() {
		for (Node n : reachableNodes(Collections.singleton(callEntry))) {
			if (n instanceof Branch) {
				Branch b = (Branch) n;
				Boolean inline = b.canBeInlined();
				if (inline != null) {
					// we can transform this to an unconditional jump
					b.inline(inline.booleanValue());
				}
			}
		}
	}

	private void makeBlocks() {
		for (Node n : reachableNodes(Collections.singleton(callEntry))) {
			if (n instanceof Target) {
				Target t = (Target) n;
				if (t.next() instanceof Linear) {
					// only insert blocks where they have a chance to grow
					LinearSeq block = new LinearSeq();
					block.insertAfter(t);
					block.grow();
				}
			}
		}
	}

	private void addResumptionPoints() {
		for (Node n : reachableNodes(Collections.singleton(callEntry))) {
			if (n instanceof AccountingNode) {
				insertResumptionAfter((AccountingNode) n);
			}
		}
	}

	private void dissolveBlocks() {
		applyTransformation(new LinearSeqTransformation() {
			@Override
			public void apply(LinearSeq seq) {
				seq.dissolve();
			}
		});
	}

	public void updateReachability() {
		reachabilityGraph = reachabilityEdges(Collections.singleton(callEntry));
	}

	public void insertCaptureNodes() {
		for (Node n : reachabilityGraph.keySet()) {
			if (n instanceof Sink && !(n instanceof LocalVariableEffect)) {
				Slots s_n = n.inSlots();

				if (s_n != null) {
					IntBuffer uncaptured = new IntBuffer();

					for (Node m : reachabilityGraph.get(n).out) {
						Slots s_m = m.inSlots();

						for (int i = 0; i < s_n.size(); i++) {
							// FIXME: double-check this condition
							if (s_n.isValidIndex(i) && s_m.isValidIndex(i)) {
								if (!s_n.getState(i).isCaptured() && s_m.getState(i).isCaptured()) {
									// need to capture i
									uncaptured.append(i);
								}
							}
						}
					}

					if (!uncaptured.isEmpty()) {
						Capture captureNode = new Capture(uncaptured.toVector());
						captureNode.insertBefore((Sink) n);
					}
				}
			}
		}
	}

	public void insertResumptionAfter(Linear n) {
		ResumptionPoint resume = new ResumptionPoint();
		resume.insertAfter(n);
		resumePoints.add(resume);
	}

	public static class Edges {
		// FIXME: may in principle be multisets
		public final Set<Node> in;
		public final Set<Node> out;

		public Edges() {
			this.in = new HashSet<>();
			this.out = new HashSet<>();
		}
	}

	private void clearSlots() {
		for (Node n : reachableNodes(Collections.singleton(callEntry))) {
			n.clearSlots();
		}
	}

	private boolean joinWith(Node n, Slots addIn) {
		Check.notNull(n);
		Check.notNull(addIn);
		return n.pushSlots(addIn);
	}

	public void updateDataFlow() {
		Map<Node, Edges> edges = reachabilityEdges(Collections.singleton(callEntry));

		clearSlots();

		Queue<Node> workList = new ArrayDeque<>();

		// push entry point's slots to the immediate successors
		for (Node n : edges.get(callEntry).out) {
			if (n.pushSlots(callEntry.outSlots())) {
				workList.add(n);
			}
		}

		while (!workList.isEmpty()) {
			Node n = workList.remove();
			assert (n != null);

			assert (n.inSlots() != null);

			// compute effect and push it to outputs
			Slots o = n.outSlots();

			for (Node m : edges.get(n).out) {
				if (m.pushSlots(o)) {
					workList.add(m);
				}
			}

		}
	}

	private Map<Node, Edges> reachabilityEdges(Iterable<Entry> entryPoints) {
		final Map<Node, Integer> timesVisited = new HashMap<>();
		final Map<Node, Edges> edges = new HashMap<>();

		NodeVisitor visitor = new NodeVisitor() {

			@Override
			public boolean visitNode(Node node) {
				if (timesVisited.containsKey(node)) {
					timesVisited.put(node, timesVisited.get(node) + 1);
					return false;
				}
				else {
					timesVisited.put(node, 1);
					if (!edges.containsKey(node)) {
						edges.put(node, new Edges());
					}
					return true;
				}
			}

			@Override
			public void visitEdge(Node from, Node to) {
				if (!edges.containsKey(from)) {
					edges.put(from, new Edges());
				}
				if (!edges.containsKey(to)) {
					edges.put(to, new Edges());
				}

				Edges fromEdges = edges.get(from);
				Edges toEdges = edges.get(to);

				fromEdges.out.add(to);
				toEdges.in.add(from);
			}
		};

		for (Entry entry : entryPoints) {
			entry.accept(visitor);
		}

		return Collections.unmodifiableMap(edges);
	}

	private void printNodes(Iterable<Entry> entryPoints) {
		ArrayList<Node> nodes = new ArrayList<>();
		Map<Node, Edges> edges = reachabilityEdges(entryPoints);

		for (Node n : edges.keySet()) {
			nodes.add(n);
		}

		System.out.println("[");
		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			Edges e = edges.get(n);

			System.out.print("\t" + i + ": ");
			System.out.print("{ ");
			for (Node m : e.in) {
				int idx = nodes.indexOf(m);
				System.out.print(idx + " ");
			}
			System.out.print("} -> ");

			System.out.print(n.toString());

			System.out.print(" -> { ");
			for (Node m : e.out) {
				int idx = nodes.indexOf(m);
				System.out.print(idx + " ");
			}
			System.out.print("}");
			System.out.println();
		}
		System.out.println("]");
	}

	private Iterable<Node> reachableNodes(Iterable<Entry> entryPoints) {
		return reachability(entryPoints).keySet();
	}

	private Map<Node, Integer> reachability(Iterable<Entry> entryPoints) {
		final Map<Node, Integer> inDegree = new HashMap<>();

		NodeVisitor visitor = new NodeVisitor() {

			@Override
			public boolean visitNode(Node n) {
				if (inDegree.containsKey(n)) {
					inDegree.put(n, inDegree.get(n) + 1);
					return false;
				}
				else {
					inDegree.put(n, 1);
					return true;
				}
			}

			@Override
			public void visitEdge(Node from, Node to) {
				// no-op
			}

		};

		for (Entry entry : entryPoints) {
			entry.accept(visitor);
		}
		return Collections.unmodifiableMap(inDegree);
	}

}
