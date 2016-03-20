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
import net.sandius.rembulan.compiler.types.FunctionType;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.compiler.types.TypeSeq;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Graph;
import net.sandius.rembulan.util.IntBuffer;
import net.sandius.rembulan.util.Ptr;
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

public class FunctionCode {

	private final Prototype prototype;
	private final TypeSeq parameterTypes;

	TypeSeq returnTypes;

	public Entry callEntry;
	public Set<ResumptionPoint> resumePoints;

	protected FunctionCode(Prototype prototype, TypeSeq parameterTypes) {
		this.prototype = Check.notNull(prototype);
		this.parameterTypes = Check.notNull(parameterTypes);
	}

	public static TypeSeq genericParameterTypes(int numOfFixedParameters, boolean vararg) {
		Type[] types = new Type[numOfFixedParameters];
		for (int i = 0; i < types.length; i++) {
			types[i] = LuaTypes.DYNAMIC;
		}
		return TypeSeq.of(ReadOnlyArray.wrap(types), vararg);
	}

	public TypeSeq parameterTypes() {
		return parameterTypes;
	}

	public TypeSeq returnTypes() {
		return returnTypes;
	}

	public FunctionType functionType() {
		return FunctionType.of(parameterTypes(), returnTypes());
	}

	private void collectCallSite(Node n, Map<Prototype, Set<TypeSeq>> callSites) {
		if (n instanceof LinearSeq) {
			for (Node m : ((LinearSeq) n).nodes()) {
				collectCallSite(m, callSites);
			}
		}
		else if (n instanceof LuaInstruction.CallInstruction) {
			LuaInstruction.CallInstruction c = (LuaInstruction.CallInstruction) n;

			Slot target = c.callTarget();

			if (target.type() instanceof FunctionType && target.origin() instanceof Origin.Closure) {
				Prototype proto = prototype.getNestedPrototypes().get(((Origin.Closure) target.origin()).index);
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

	public Map<Prototype, Set<TypeSeq>> callSites() {
		final Map<Prototype, Set<TypeSeq>> callSites = new HashMap<>();

		Nodes.traverseOnce(callEntry, new NodeAction() {
			@Override
			public void visit(Node n) {
				collectCallSite(n, callSites);
			}
		});

		return callSites;
	}

	public Graph<Node> nodeGraph() {
		return Nodes.toGraph(callEntry);
	}

	private class Pusher extends Nodes.NodeSuccessorAction {
		private final Queue<Node> workList;
		public Pusher(Node n, Queue<Node> workList) {
			super(n);
			this.workList = workList;
		}

		@Override
		public void visitSuccessor(Node node) {
			if (node.pushSlots(selfNode().outSlots())) {
				workList.add(node);
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
			Type targetType = ((ReturnType.TailCallReturnType) rt).target;
			if (targetType instanceof FunctionType) {
				FunctionType ft = (FunctionType) targetType;
				return ft.returnTypes();
			}
			else {
				return TypeSeq.vararg();
			}
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

		returnTypes = !ret.isNull() ? ret.get() : TypeSeq.vararg();
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

	public Iterable<Node> successors(Node n) {
		final Set<Node> result = new HashSet<>();
		n.accept(new Nodes.NodeSuccessorAction(n) {
			@Override
			public void visitSuccessor(Node node) {
				result.add(node);
			}
		});
		return result;
	}

	public void insertCaptureNodes() {
		Nodes.traverseOnce(callEntry, new NodeAction() {
			@Override
			public void visit(Node n) {
				if (n instanceof Sink && !(n instanceof LocalVariableEffect)) {
					SlotState s_n = n.inSlots();

					if (s_n != null) {
						IntBuffer uncaptured = IntBuffer.empty();

						for (Node m : successors(n)) {
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
		});
	}

	public void insertResumptionAfter(Linear n) {
		ResumptionPoint resume = new ResumptionPoint();
		resume.insertAfter(n);
		resumePoints.add(resume);
	}

	public Iterable<Node> sortTopologically() {
		final List<Node> cont = new ArrayList<>();
		final Set<Node> visited = new HashSet<>();

		callEntry.accept(new NodeVisitor() {
			@Override
			public boolean visitNode(Node node) {
				if (!visited.contains(node)) {
					visited.add(node);
					if (node instanceof LinearSeq) {
						LinearSeq seq = (LinearSeq) node;
						for (Node n : seq.nodes()) {
							cont.add(n);
						}
					}
					else {
						cont.add(node);
					}
					return true;
				}
				else {
					return false;
				}
			}
		});

		return Collections.unmodifiableList(cont);
	}

}
