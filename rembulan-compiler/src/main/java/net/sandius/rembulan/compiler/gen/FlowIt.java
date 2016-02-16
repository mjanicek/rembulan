package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.AccountingNode;
import net.sandius.rembulan.compiler.gen.block.Branch;
import net.sandius.rembulan.compiler.gen.block.Capture;
import net.sandius.rembulan.compiler.gen.block.Entry;
import net.sandius.rembulan.compiler.gen.block.Exit;
import net.sandius.rembulan.compiler.gen.block.HookNode;
import net.sandius.rembulan.compiler.gen.block.LineInfo;
import net.sandius.rembulan.compiler.gen.block.Linear;
import net.sandius.rembulan.compiler.gen.block.LinearSeq;
import net.sandius.rembulan.compiler.gen.block.LinearSeqTransformation;
import net.sandius.rembulan.compiler.gen.block.LocalVariableEffect;
import net.sandius.rembulan.compiler.gen.block.LuaInstruction;
import net.sandius.rembulan.compiler.gen.block.Node;
import net.sandius.rembulan.compiler.gen.block.NodeAppender;
import net.sandius.rembulan.compiler.gen.block.NodeVisitor;
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
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class FlowIt {

	private final Map<Prototype, Unit> units;

	@Deprecated
	private final Unit mainUnit;

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
		Prototype prototype = unit.prototype;
		CompiledPrototype cp = new CompiledPrototype(prototype, unit.genericParameters());

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

		cp.returnType = TypeSeq.vararg();

		cp.callEntry = new Entry("main", unit.genericParameters(), prototype.getMaximumStackSize(), pcLabels.get(0));

		cp.resumePoints = new HashSet<>();

		cp.callSites = new HashMap<>();

		cp.insertHooks();

		cp.inlineInnerJumps();
		cp.makeBlocks();

		cp.applyTransformation(new CollectCPUAccounting());

		// remove repeated line info nodes
		cp.applyTransformation(new RemoveRedundantLineNodes());

		// dissolve blocks
		cp.dissolveBlocks();

		// remove all line info nodes
//		applyTransformation(entryPoints, new LinearSeqTransformation.Remove(Predicates.isClass(LineInfo.class)));

//		System.out.println();
//		printNodes(entryPoints);

		cp.updateReachability();
		cp.updateDataFlow();

		cp.inlineBranches();

		// add capture nodes
		cp.insertCaptureNodes();

//		addResumptionPoints();

		cp.computeCallSites();

		cp.makeBlocks();

		cp.updateReachability();
		cp.updateDataFlow();

		cp.computeReturnType();

		unit.setGeneric(cp);
	}

	@Deprecated
	public Type.FunctionType functionType() {
		return mainUnit.generic().functionType();
	}

	@Deprecated
	public Map<Node, CompiledPrototype.Edges> reachabilityGraph() {
		return mainUnit.generic().reachabilityGraph;
	}

	@Deprecated
	public Map<Prototype, Set<TypeSeq>> callSites() {
		return mainUnit.generic().callSites;
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
