package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.AccountingNode;
import net.sandius.rembulan.compiler.gen.block.LineInfo;
import net.sandius.rembulan.compiler.gen.block.Linear;
import net.sandius.rembulan.compiler.gen.block.LinearSeq;
import net.sandius.rembulan.compiler.gen.block.LinearSeqTransformation;
import net.sandius.rembulan.compiler.gen.block.Nodes;
import net.sandius.rembulan.lbc.Prototype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChunkCompiler {

	public ChunkCompiler() {
	}

	public Chunk compile(Prototype prototype, String name) {
		Map<Prototype, CompilationUnit> units = new HashMap<>();
		List<CompilationUnit> us = new LinkedList<>();
		List<CompiledClass> classes = new LinkedList<>();

		CompilationContext ctx = new CompilationContext(units);

		ClassNameGenerator nameGenerator = new BaseClassNameGenerator(name);

		addUnits(units, ctx, prototype, null, us, nameGenerator);

		for (CompilationUnit u : us) {
			System.out.println("Processing " + u.name() + "...");
			processGeneric(u);

			CompiledClass ccl = u.toCompiledClass();
			if (ccl != null) {
				classes.add(ccl);
			}
			else {
				System.out.println("No compiled bytecode for class " + u.name());
			}
		}

		return new Chunk(prototype, units, classes);
	}

	private void addUnits(Map<Prototype, CompilationUnit> units, CompilationContext ctx, Prototype prototype, Prototype parent, List<CompilationUnit> us, ClassNameGenerator nameGen) {
		if (!units.containsKey(prototype)) {
			CompilationUnit u = new CompilationUnit(prototype, parent, nameGen.next(), ctx);
			u.initGeneric();

			units.put(prototype, u);
			us.add(0, u);  // prepending in order to ensure that nested prototypes are processed before their parent

			ClassNameGenerator childNameGen = nameGen.childGenerator();

			for (int i = 0; i < prototype.getNestedPrototypes().size(); i++) {
				Prototype np = prototype.getNestedPrototypes().get(i);
				addUnits(units, ctx, np, prototype, us, childNameGen);
			}
		}
	}

	private void processGeneric(CompilationUnit compilationUnit) {
		FunctionCode cp = compilationUnit.generic();

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
