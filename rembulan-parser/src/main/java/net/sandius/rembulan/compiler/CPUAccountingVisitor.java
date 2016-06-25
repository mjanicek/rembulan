package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.analysis.BlockTransformerVisitor;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.CPUWithdraw;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.util.DefaultNodeActionVisitor;
import net.sandius.rembulan.util.Check;

import java.util.Iterator;

public class CPUAccountingVisitor extends BlockTransformerVisitor {

	private final NodeAccountingVisitor accounter;

	private static class NodeAccountingVisitor extends DefaultNodeActionVisitor {

		private int cost;

		public int cost() {
			return cost;
		}

		public void reset() {
			cost = 0;
		}

		@Override
		protected void action(IRNode node) {
			cost += 1;
		}

		@Override
		public void visit(CPUWithdraw node) {
			cost += node.cost();
		}

	}

	public CPUAccountingVisitor(NodeAccountingVisitor visitor) {
		super(Check.notNull(visitor));
		this.accounter = visitor;
	}

	public CPUAccountingVisitor() {
		this(new NodeAccountingVisitor());
	}

	private static void removeCPUNodes(Iterable<BodyNode> nodes) {
		Iterator<BodyNode> it = nodes.iterator();
		while (it.hasNext()) {
			BodyNode n = it.next();
			if (n instanceof CPUWithdraw) {
				it.remove();
			}
		}
	}

	@Override
	public void postVisit(BasicBlock block) {
		try {
			int cost = accounter.cost();
			removeCPUNodes(currentBody());
			if (cost > 0) {
				currentBody().add(0, new CPUWithdraw(cost));
			}
		}
		finally {
			accounter.reset();
		}
	}

}
