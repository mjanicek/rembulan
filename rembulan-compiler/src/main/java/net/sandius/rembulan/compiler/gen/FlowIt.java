package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.AccountingNode;
import net.sandius.rembulan.compiler.gen.block.Entry;
import net.sandius.rembulan.compiler.gen.block.LineInfo;
import net.sandius.rembulan.compiler.gen.block.Linear;
import net.sandius.rembulan.compiler.gen.block.LinearPredicate;
import net.sandius.rembulan.compiler.gen.block.LinearSeq;
import net.sandius.rembulan.compiler.gen.block.LinearSeqTransformation;
import net.sandius.rembulan.compiler.gen.block.Node;
import net.sandius.rembulan.compiler.gen.block.NodeVisitor;
import net.sandius.rembulan.compiler.gen.block.Nodes;
import net.sandius.rembulan.compiler.gen.block.Predicates;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.compiler.gen.block.UnconditionalJump;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlowIt {

	public final Prototype prototype;

	public FlowIt(Prototype prototype) {
		this.prototype = prototype;
	}

	public Map<Node, Edges> go() {
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

		Entry callEntry = new Entry("main", pcLabels.get(0));

		Set<Entry> entryPoints = new HashSet<>();
		entryPoints.add(callEntry);

		inlineInnerJumps(entryPoints);
		makeBlocks(entryPoints);

		applyTransformation(entryPoints, new CollectCPUAccounting());

		// remove line info nodes
//		applyTransformation(entryPoints, new LinearSeqTransformation.Remove(Predicates.isClass(LineInfo.class)));

//		System.out.println();
//		printNodes(entryPoints);

		return reachabilityEdges(entryPoints);
	}

	private static class CollectCPUAccounting extends LinearSeqTransformation {

		@Override
		public void apply(LinearSeq seq) {
			List<AccountingNode> accountingNodes = new ArrayList<>();

			int cost = 0;

			for (Node n : seq.nodes()) {
				if (n instanceof AccountingNode) {
					if (n instanceof AccountingNode.Tick) {
						cost += 1;
					}
					else if (n instanceof AccountingNode.Sum) {
						cost += ((AccountingNode.Sum) n).cost;
					}
					accountingNodes.add((AccountingNode) n);
				}
			}

			for (AccountingNode an : accountingNodes) {
				// remove all nodes
				an.remove();
			}

			if (cost > 0) {
				// insert cost node at the beginning
				seq.insertAtBeginning(new AccountingNode.Sum(cost));
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

	private void inlineInnerJumps(Iterable<Entry> entryPoints) {
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

	private void makeBlocks(Iterable<Entry> entryPoints) {
		for (Node n : reachableNodes(entryPoints)) {
			if (n instanceof Target) {
				Target t = (Target) n;
				LinearSeq block = new LinearSeq();
				block.insertAfter(t);
				block.grow();
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
