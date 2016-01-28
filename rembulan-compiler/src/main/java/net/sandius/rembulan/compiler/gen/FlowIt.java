package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.AccountingNode;
import net.sandius.rembulan.compiler.gen.block.Capture;
import net.sandius.rembulan.compiler.gen.block.Entry;
import net.sandius.rembulan.compiler.gen.block.LineInfo;
import net.sandius.rembulan.compiler.gen.block.Linear;
import net.sandius.rembulan.compiler.gen.block.LinearSeq;
import net.sandius.rembulan.compiler.gen.block.LinearSeqTransformation;
import net.sandius.rembulan.compiler.gen.block.LocalVariableEffect;
import net.sandius.rembulan.compiler.gen.block.Node;
import net.sandius.rembulan.compiler.gen.block.NodeVisitor;
import net.sandius.rembulan.compiler.gen.block.Nodes;
import net.sandius.rembulan.compiler.gen.block.Sink;
import net.sandius.rembulan.compiler.gen.block.SlotEffect;
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
	public Set<Entry> entryPoints;

	public Map<Node, Edges> reachabilityGraph;
	public Map<Node, Slots> slots;

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

		LuaInstructionToNodeTranslator translator = new LuaInstructionToNodeTranslator();

		for (int pc = 0; pc < pcLabels.size(); pc++) {
			translator.translate(code.get(pc), pc, prototype.getLineAtPC(pc), pcLabels);
		}

//		System.out.println("[");
//		for (int i = 0; i < pcLabels.size(); i++) {
//			NLabel label = pcLabels.get(i);
//			System.out.println(i + ": " + label.toString());
//		}
//		System.out.println("]");

		callEntry = new Entry("main", pcLabels.get(0));

		entryPoints = new HashSet<>();
		entryPoints.add(callEntry);

		inlineInnerJumps();
		makeBlocks();

		applyTransformation(entryPoints, new CollectCPUAccounting());

		// remove repeated line info nodes
		applyTransformation(entryPoints, new RemoveRedundantLineNodes());

		// dissolve blocks
		dissolveBlocks(entryPoints);

		// remove all line info nodes
//		applyTransformation(entryPoints, new LinearSeqTransformation.Remove(Predicates.isClass(LineInfo.class)));

//		System.out.println();
//		printNodes(entryPoints);

		updateReachability();
		updateDataFlow();

		// add capture nodes
		insertCaptureNodes();

		updateReachability();
		updateDataFlow();

	}

	private static class CollectCPUAccounting extends LinearSeqTransformation {

		@Override
		public void apply(LinearSeq seq) {
			List<AccountingNode> toBeRemoved = new ArrayList<>();

			int cost = 0;

			for (Linear n : seq.nodes()) {
				if (n instanceof AccountingNode) {
					AccountingNode an = (AccountingNode) n;
					if (n instanceof AccountingNode.Tick) {
						cost += 1;
						toBeRemoved.add(an);
					}
					else if (n instanceof AccountingNode.Sum) {
						cost += ((AccountingNode.Sum) n).cost;
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
				seq.insertAtBeginning(new AccountingNode.Sum(cost));
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

	private void applyTransformation(Iterable<Entry> entryPoints, LinearSeqTransformation tf) {
		for (Node n : reachableNodes(entryPoints)) {
			if (n instanceof LinearSeq) {
				LinearSeq seq = (LinearSeq) n;
				seq.apply(tf);
			}
		}
	}

	private void inlineInnerJumps() {
		for (Node n : reachableNodes(entryPoints)) {
			if (n instanceof Target) {
				Target t = (Target) n;
				UnconditionalJump jmp = t.optIncomingJump();
				if (jmp != null) {
					Nodes.inline(jmp);
				}
			}
		}
	}

	private void makeBlocks() {
		for (Node n : reachableNodes(entryPoints)) {
			if (n instanceof Target) {
				Target t = (Target) n;
				LinearSeq block = new LinearSeq();
				block.insertAfter(t);
				block.grow();
			}
		}
	}

	private void dissolveBlocks(Iterable<Entry> entryPoints) {
		applyTransformation(entryPoints, new LinearSeqTransformation() {
			@Override
			public void apply(LinearSeq seq) {
				seq.dissolve();
			}
		});
	}

	public void updateReachability() {
		reachabilityGraph = reachabilityEdges(entryPoints);
	}

	public void insertCaptureNodes() {
		for (Node n : reachabilityGraph.keySet()) {
			if (n instanceof Sink && !(n instanceof LocalVariableEffect)) {
				Slots s_n = slots.get(n);

				if (s_n != null) {
					IntBuffer uncaptured = new IntBuffer();

					for (Node m : reachabilityGraph.get(n).out) {
						Slots s_m = slots.get(m);

						for (int i = 0; i < s_n.size(); i++) {
							if (!s_n.getState(i).isCaptured() && s_m.getState(i).isCaptured()) {
								// need to capture i
								uncaptured.append(i);
//								System.out.println("need to capture " + i + " in " + n);
							}
						}
					}

					for (int i = 0; i < uncaptured.length(); i++) {
						int index = uncaptured.get(i);

						Capture captureNode = new Capture(index);
						captureNode.insertBefore((Sink) n);

//						System.out.println("adding " + captureNode);
					}
				}
			}
		}
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

	private Map<Node, Slots> initSlots(Entry entryPoint) {
		Map<Node, Slots> slots = new HashMap<>();
		for (Node n : reachableNodes(Collections.singleton(entryPoint))) {
			slots.put(n, null);
		}
		return slots;
	}

	private Slots effect(Node n, Slots in) {
		if (n instanceof SlotEffect) {
			SlotEffect eff = (SlotEffect) n;
			return eff.effect(in, prototype);
		}
		else {
			return in;
		}
	}

	private boolean joinWith(Map<Node, Slots> slots, Node n, Slots addIn) {
		Check.notNull(slots);
		Check.notNull(addIn);

		Slots oldIn = slots.get(n);
		Slots newIn = oldIn == null ? addIn : oldIn.join(addIn);
		if (!newIn.equals(oldIn)) {
			slots.put(n, newIn);
			return true;
		}
		else {
			return false;
		}
	}

	public void updateDataFlow() {
		slots = dataFlow(callEntry);
	}

	public Map<Node, Slots> dataFlow(Entry entryPoint) {
		Map<Node, Edges> edges = reachabilityEdges(Collections.singleton(entryPoint));
		Map<Node, Slots> slots = initSlots(entryPoint);

		Slots entrySlots = entrySlots();

		Queue<Node> workList = new ArrayDeque<>();

		// push entry point's slots to the immediate successors
		for (Node n : edges.get(entryPoint).out) {
			if (joinWith(slots, n, entrySlots)) {
				workList.add(n);
			}
		}

		while (!workList.isEmpty()) {
			Node n = workList.remove();
			assert (n != null);

			assert (slots.get(n) != null);

			// compute effect and push it to outputs
			Slots o = effect(n, slots.get(n));
			for (Node m : edges.get(n).out) {
				if (joinWith(slots, m, o)) {
					workList.add(m);
				}
			}

		}

		return slots;
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

	private Slots entrySlots() {
		Slots s = Slots.init(prototype.getMaximumStackSize());
		for (int i = 0; i < prototype.getNumberOfParameters(); i++) {
			s = s.updateType(i, Slots.SlotType.ANY);
		}
		return s;
	}

}
