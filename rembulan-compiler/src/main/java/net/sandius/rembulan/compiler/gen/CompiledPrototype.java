package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.AccountingNode;
import net.sandius.rembulan.compiler.gen.block.Branch;
import net.sandius.rembulan.compiler.gen.block.Capture;
import net.sandius.rembulan.compiler.gen.block.Entry;
import net.sandius.rembulan.compiler.gen.block.Exit;
import net.sandius.rembulan.compiler.gen.block.HookNode;
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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class CompiledPrototype {

	private final Prototype prototype;
	private final TypeSeq actualParameters;

	public TypeSeq returnType;

	public Entry callEntry;
	public Set<ResumptionPoint> resumePoints;

	public Map<Node, Edges> reachabilityGraph;

	public Map<Prototype, Set<TypeSeq>> callSites;

	public static class Edges {
		// FIXME: may in principle be multisets
		public final Set<Node> in;
		public final Set<Node> out;

		public Edges() {
			this.in = new HashSet<>();
			this.out = new HashSet<>();
		}
	}

	protected CompiledPrototype(Prototype prototype, TypeSeq actualParameters) {
		this.prototype = Objects.requireNonNull(prototype);
		this.actualParameters = Objects.requireNonNull(actualParameters);
	}

	public TypeSeq actualParameters() {
		return actualParameters;
	}

	public TypeSeq returnType() {
		return returnType;
	}

	public Type.FunctionType functionType() {
		return Type.FunctionType.of(actualParameters(), returnType());
	}

	private void addCallSite(Prototype target, TypeSeq args) {
		Objects.requireNonNull(target);
		Objects.requireNonNull(args);

		Set<TypeSeq> cs = callSites.get(target);

		if (cs != null) {
			cs.add(args);
		}
		else {
			Set<TypeSeq> s = new HashSet<>();
			s.add(args);

			callSites.put(target, s);
		}
	}

	public void computeCallSites() {
		callSites.clear();

		for (Node n : reachableNodes(Collections.singleton(callEntry))) {
			if (n instanceof LuaInstruction.Call) {
				LuaInstruction.Call c = (LuaInstruction.Call) n;

				Slot target = c.callTarget(c.inSlots());

				if (target.type() instanceof Type.FunctionType && target.origin() instanceof Origin.Closure) {
					Prototype proto = ((Origin.Closure) target.origin()).prototype;
					TypeSeq args = c.callArguments(c.inSlots());
					addCallSite(proto, args);
				}
			}
			// FIXME: remove code duplication -- this could simply be an interface
			else if (n instanceof LuaInstruction.TailCall) {
				LuaInstruction.TailCall tc = (LuaInstruction.TailCall) n;

				Slot target = tc.callTarget(tc.inSlots());

				if (target.type() instanceof Type.FunctionType && target.origin() instanceof Origin.Closure) {
					Prototype proto = ((Origin.Closure) target.origin()).prototype;
					TypeSeq args = tc.callArguments(tc.inSlots());
					addCallSite(proto, args);
				}
			}
		}
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
			SlotState o = n.outSlots();

			for (Node m : edges.get(n).out) {
				if (m.pushSlots(o)) {
					workList.add(m);
				}
			}

		}
	}

	private void clearSlots() {
		for (Node n : reachableNodes(Collections.singleton(callEntry))) {
			n.clearSlots();
		}
	}

	public void updateReachability() {
		reachabilityGraph = reachabilityEdges(Collections.singleton(callEntry));
	}

	private static TypeSeq returnTypeToArgTypes(ReturnType rt) {
		if (rt instanceof ReturnType.ConcreteReturnType) {
			return ((ReturnType.ConcreteReturnType) rt).typeSeq;
		}
		else if (rt instanceof ReturnType.TailCallReturnType) {
			return TypeSeq.vararg();  // TODO
		}
		else {
			throw new IllegalStateException("unknown return type: " + rt.toString());
		}
	}

	public void computeReturnType() {
		TypeSeq ret = null;

		for (Node n : reachableNodes(Collections.singleton(callEntry))) {
			if (n instanceof Exit) {
				TypeSeq at = returnTypeToArgTypes(((Exit) n).returnType());
				ret = ret != null ? ret.join(at) : at;
			}
		}

		returnType = ret != null ? ret : TypeSeq.vararg();
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

	public void applyTransformation(LinearSeqTransformation tf) {
		for (Node n : reachableNodes(Collections.singleton(callEntry))) {
			if (n instanceof LinearSeq) {
				LinearSeq seq = (LinearSeq) n;
				seq.apply(tf);
			}
		}
	}

	public void inlineInnerJumps() {
		for (Node n : reachableNodes(Collections.singleton(callEntry))) {
			if (n instanceof UnconditionalJump) {
				((UnconditionalJump) n).tryInlining();
			}
		}
	}

	public void inlineBranches() {
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

	public void makeBlocks() {
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

	public void dissolveBlocks() {
		applyTransformation(new LinearSeqTransformation() {
			@Override
			public void apply(LinearSeq seq) {
				seq.dissolve();
			}
		});
	}

	public void insertCaptureNodes() {
		for (Node n : reachabilityGraph.keySet()) {
			if (n instanceof Sink && !(n instanceof LocalVariableEffect)) {
				SlotState s_n = n.inSlots();

				if (s_n != null) {
					IntBuffer uncaptured = new IntBuffer();

					for (Node m : reachabilityGraph.get(n).out) {
						SlotState s_m = m.inSlots();

						for (int i = 0; i < s_n.size(); i++) {
							// FIXME: double-check this condition
							if (s_n.isValidIndex(i) && s_m.isValidIndex(i)) {
								if (!s_n.isCaptured(i) && s_m.isCaptured(i)) {
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

	private boolean joinWith(Node n, SlotState addIn) {
		Check.notNull(n);
		Check.notNull(addIn);
		return n.pushSlots(addIn);
	}

}
