package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.AccountingNode;
import net.sandius.rembulan.compiler.gen.block.Entry;
import net.sandius.rembulan.compiler.gen.block.LineInfo;
import net.sandius.rembulan.compiler.gen.block.Linear;
import net.sandius.rembulan.compiler.gen.block.LinearSeq;
import net.sandius.rembulan.compiler.gen.block.LinearSeqTransformation;
import net.sandius.rembulan.compiler.gen.block.Node;
import net.sandius.rembulan.compiler.gen.block.Nodes;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Graph;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FlowIt {

	private final Map<Prototype, Unit> units;

	@Deprecated
	private final Unit mainUnit;

	@Deprecated
	private Map<Prototype, Set<TypeSeq>> callSites;

	public FlowIt(Prototype prototype) {
		Objects.requireNonNull(prototype);

		units = new HashMap<>();

		mainUnit = new Unit(prototype);
		units.put(prototype, mainUnit);
	}

	@Deprecated
	public void go() {
		goGeneric(mainUnit);
	}

	public void goGeneric(Unit unit) {
		CompiledPrototype cp = unit.makeCompiledPrototype(unit.genericParameters());

		cp.insertHooks();

		cp.inlineInnerJumps();
		cp.makeBlocks();

		Nodes.applyTransformation(cp.callEntry, new CollectCPUAccounting());

		// remove repeated line info nodes
		Nodes.applyTransformation(cp.callEntry, new RemoveRedundantLineNodes());

		// dissolve blocks
		cp.dissolveBlocks();

		// remove all line info nodes
//		applyTransformation(entryPoints, new LinearSeqTransformation.Remove(Predicates.isClass(LineInfo.class)));

//		System.out.println();
//		printNodes(entryPoints);

		cp.updateDataFlow();

		cp.inlineBranches();

		// add capture nodes
		cp.insertCaptureNodes();

//		addResumptionPoints();

		Map<Prototype, Set<TypeSeq>> callSites = cp.callSites();
		this.callSites = callSites;

		cp.makeBlocks();

		cp.updateDataFlow();

		cp.computeReturnType();

		unit.setGeneric(cp);
	}

	@Deprecated
	public Type.FunctionType functionType() {
		return mainUnit.generic().functionType();
	}

	@Deprecated
	public Graph<Node> nodeGraph() {
		return mainUnit.generic().nodeGraph();
	}

	@Deprecated
	public Map<Prototype, Set<TypeSeq>> callSites() {
		return callSites;
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

}
