package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Graph;
import net.sandius.rembulan.util.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Nodes {

	private Nodes() {
		// not to be instantiated
	}

	public static Target makeTarget(Sink n) {
		Check.notNull(n);

		if (n instanceof Target) {
			return (Target) n;
		}
		else {
			Target target = new Target();
			UnconditionalJump jump = new UnconditionalJump(target);

			Src prev = n.prev();
			prev.appendSink(jump);
			n.prependSource(target);

			return target;
		}
	}

	public static void inline(UnconditionalJump jmp) {
		Target tgt = jmp.target();
		if (tgt.optIncomingJump() != jmp) {
			throw new IllegalArgumentException("Jump destination cannot be inlined: " + tgt);
		}

		Src src = jmp.prev();
		Sink dest = jmp.target().next();

		jmp.target().dec(jmp);

		src.setNext(dest);
		dest.setPrev(src);
	}

	// should not modify the underlying graph!
	public static void traverseOnce(Node from, final NodeAction na) {
		final Set<Node> visited = new HashSet<>();

		NodeVisitor nv = new NodeVisitor() {
			@Override
			public boolean visitNode(Node node) {
				if (visited.contains(node)) {
					return false;
				}
				else {
					visited.add(node);
					na.visit(node);
					return true;
				}
			}
		};

		from.accept(nv);
	}

	public static void applyTransformation(Node from, final LinearSeqTransformation tf) {
		Nodes.traverseOnce(from, new NodeAction() {
			@Override
			public void visit(Node n) {
				if (n instanceof LinearSeq) {
					LinearSeq seq = (LinearSeq) n;
					seq.apply(tf);
				}
			}
		});
	}

	public static Set<Node> reachableNodes(Node from) {
		final Set<Node> visited = new HashSet<>();

		NodeVisitor visitor = new NodeVisitor() {
			@Override
			public boolean visitNode(Node n) {
				if (visited.contains(n)) {
					return false;
				}
				else {
					visited.add(n);
					return true;
				}
			}
		};

		from.accept(visitor);

		return Collections.unmodifiableSet(visited);
	}

	public static Graph<Node> toGraph(Node from) {

		final Set<Node> vertices = new HashSet<>();
		final Set<Pair<Node, Node>> edges = new HashSet<>();

		NodeVisitor visitor = new NodeVisitor() {

			@Override
			public boolean visitNode(Node node) {
				if (vertices.contains(node)) {
					return false;
				}
				else {
					vertices.add(node);
					return true;
				}
			}

			@Override
			public void visitEdge(Node from, Node to) {
				Pair<Node, Node> edge = new Pair<>(from, to);
				edges.add(edge);
			}
		};

		from.accept(visitor);

		return Graph.wrap(Collections.unmodifiableSet(vertices), Collections.unmodifiableSet(edges));
	}

	// perform an action in all successors of the node n
	public abstract static class NodeSuccessorAction extends NodeVisitor {

		private final Node n;

		public NodeSuccessorAction(Node n) {
			this.n = n;
		}

		public abstract void visitSuccessor(Node node);

		protected Node selfNode() {
			return n;
		}

		@Override
		public boolean visitNode(Node node) {
			if (node == n) {
				return true;
			}
			else {
				visitSuccessor(node);
				return false;
			}
		}

	}

}
