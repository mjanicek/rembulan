package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.AccountingNode;
import net.sandius.rembulan.compiler.gen.block.LineInfo;
import net.sandius.rembulan.compiler.gen.block.Linear;
import net.sandius.rembulan.compiler.gen.block.LinearSeq;
import net.sandius.rembulan.compiler.gen.block.LinearSeqTransformation;
import net.sandius.rembulan.compiler.gen.block.Nodes;
import net.sandius.rembulan.lbc.Prototype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FlowIt {

	private final Prototype prototype;

	private final ClassNameGenerator nameGenerator;

	private final Map<Prototype, Unit> units;

	public FlowIt(Prototype prototype, ClassNameGenerator nameGenerator) {
		this.prototype = Objects.requireNonNull(prototype);
		this.nameGenerator = Objects.requireNonNull(nameGenerator);
		this.units = new HashMap<>();
	}

	public Iterable<Unit> units() {
		return Collections.unmodifiableCollection(units.values());
	}

	@Deprecated
	public void go() {
		addUnits(prototype, nameGenerator);

		for (Unit u : units.values()) {
			processGeneric(u);
		}
	}

	private void addUnits(Prototype prototype, ClassNameGenerator nameGen) {
		if (!units.containsKey(prototype)) {
			Unit u = initUnit(prototype, nameGen.className());
			units.put(prototype, u);

			for (int i = 0; i < prototype.getNestedPrototypes().size(); i++) {
				Prototype np = prototype.getNestedPrototypes().get(i);
				addUnits(np, nameGen.child(i));
			}
		}
	}

	private Unit initUnit(Prototype prototype, String name) {
		Unit unit = new Unit(prototype, name);
		unit.initGeneric();
		return unit;
	}

	private void processGeneric(Unit unit) {
		CompiledPrototype cp = unit.generic();

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

		cp.makeBlocks();

		cp.updateDataFlow();

		cp.computeReturnType();
	}

	public Unit mainUnit() {
		return units.get(prototype);
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
