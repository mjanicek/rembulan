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
import net.sandius.rembulan.compiler.gen.block.NodeAction;
import net.sandius.rembulan.compiler.gen.block.NodeAppender;
import net.sandius.rembulan.compiler.gen.block.NodeVisitor;
import net.sandius.rembulan.compiler.gen.block.Nodes;
import net.sandius.rembulan.compiler.gen.block.ResumptionPoint;
import net.sandius.rembulan.compiler.gen.block.Sink;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.compiler.gen.block.UnconditionalJump;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Graph;
import net.sandius.rembulan.util.IntBuffer;
import net.sandius.rembulan.util.Pair;
import net.sandius.rembulan.util.Ptr;

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

	public Map<Prototype, Set<TypeSeq>> callSites;

	@Deprecated
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

	public void computeCallSites() {
		callSites.clear();

		Nodes.traverseOnce(callEntry, new NodeAction() {
			@Override
			public void visit(Node n) {
				if (n instanceof LuaInstruction.CallInstruction) {
					LuaInstruction.CallInstruction c = (LuaInstruction.CallInstruction) n;

					Slot target = c.callTarget();

					if (target.type() instanceof Type.FunctionType && target.origin() instanceof Origin.Closure) {
						Prototype proto = ((Origin.Closure) target.origin()).prototype;
						TypeSeq args = c.callArguments();

						// add to call sites
						Set<TypeSeq> cs = callSites.get(proto);
						if (cs != null) {
							cs.add(args);
						}
						else {
							Set<TypeSeq> s = new HashSet<>();
							s.add(args);
							callSites.put(proto, s);
						}
					}
				}
			}
		});
	}

	public Graph<Node> nodeGraph() {
		return Nodes.toGraph(callEntry);
	}

	@Deprecated
	private static Map<Node, Edges> reachabilityEdges(Graph<Node> g) {

		final Map<Node, Edges> edges = new HashMap<>();

		for (Node n : g.vertices()) {
			Edges es = new Edges();
			edges.put(n, es);
		}

		for (Pair<Node, Node> e : g.edges()) {
			Node u = e.first();
			Node v = e.second();
			edges.get(u).out.add(v);
			edges.get(v).in.add(u);
		}

		return Collections.unmodifiableMap(edges);
	}

	private class Pusher extends NodeVisitor {
		private final Node n;
		private final Queue<Node> workList;
		public Pusher(Node n, Queue<Node> workList) {
			this.n = n;
			this.workList = workList;
		}

		@Override
		public boolean visitNode(Node node) {
			if (node == n) {
				return true;
			}
			else {
				if (node.pushSlots(n.outSlots())) {
					workList.add(node);
				}
				return false;
			}
		}

	}

	public void updateDataFlow() {
		clearSlots();

		Queue<Node> workList = new ArrayDeque<>();

		// push entry point's slots to the immediate successors
		callEntry.accept(new Pusher(callEntry, workList));

		while (!workList.isEmpty()) {
			Node n = workList.remove();
			assert (n != null);

			assert (n.inSlots() != null);

			// compute effect and push it to outputs
			n.accept(new Pusher(n, workList));
		}
	}

	private void clearSlots() {
		Nodes.traverseOnce(callEntry, new NodeAction() {
			@Override
			public void visit(Node n) {
				n.clearSlots();
			}
		});
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
		final Ptr<TypeSeq> ret = new Ptr<>();

		Nodes.traverseOnce(callEntry, new NodeAction() {
			@Override
			public void visit(Node n) {
				if (n instanceof Exit) {
					TypeSeq at = returnTypeToArgTypes(((Exit) n).returnType());
					ret.set(!ret.isNull() ? ret.get().join(at) : at);
				}
			}
		});

		returnType = !ret.isNull() ? ret.get() : TypeSeq.vararg();
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

	public void inlineInnerJumps() {
		Nodes.traverseOnce(callEntry, new NodeAction() {
			@Override
			public void visit(Node n) {
				if (n instanceof UnconditionalJump) {
					((UnconditionalJump) n).tryInlining();
				}
			}
		});
	}

	public void inlineBranches() {
		Nodes.traverseOnce(callEntry, new NodeAction() {
			@Override
			public void visit(Node n) {
				if (n instanceof Branch) {
					Branch b = (Branch) n;
					Branch.InlineTarget it = b.canBeInlined();
					switch (it) {
						case TRUE_BRANCH:  b.inline(true); break;
						case FALSE_BRANCH: b.inline(false); break;
						default:  // no-op
					}
				}
			}
		});
	}

	public void makeBlocks() {
		Nodes.traverseOnce(callEntry, new NodeAction() {
			@Override
			public void visit(Node n) {
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
		});
	}

	private void addResumptionPoints() {
		Nodes.traverseOnce(callEntry, new NodeAction() {
			@Override
			public void visit(Node n) {
				if (n instanceof AccountingNode) {
					insertResumptionAfter((AccountingNode) n);
				}
			}
		});
	}

	public void dissolveBlocks() {
		Nodes.applyTransformation(callEntry, new LinearSeqTransformation() {
			@Override
			public void apply(LinearSeq seq) {
				seq.dissolve();
			}
		});
	}

	public void insertCaptureNodes() {
		Map<Node, Edges> rg = reachabilityEdges(nodeGraph());

		for (Node n : rg.keySet()) {
			if (n instanceof Sink && !(n instanceof LocalVariableEffect)) {
				SlotState s_n = n.inSlots();

				if (s_n != null) {
					IntBuffer uncaptured = new IntBuffer();

					for (Node m : rg.get(n).out) {
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

}
