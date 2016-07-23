package net.sandius.rembulan.compiler.tf;

import net.sandius.rembulan.compiler.ir.BasicBlock;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.CPUWithdraw;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.util.DefaultNodeActionVisitor;
import net.sandius.rembulan.util.Check;

import java.util.Iterator;

class CPUAccountingVisitor extends CodeTransformerVisitor {

	private final Account acc;

	public static abstract class Account {

		private int cost;

		public int get() {
			return cost;
		}

		public void reset() {
			cost = 0;
		}

		protected void add(int c) {
			cost += Check.nonNegative(c);
		}

		public abstract void cpuNode(CPUWithdraw node);

		public abstract void otherNode(IRNode node);

	}

	public static final Account INITIALISE = new Account() {
		@Override
		public void cpuNode(CPUWithdraw node) {
			// no-op
		}

		@Override
		public void otherNode(IRNode node) {
			add(1);
		}
	};

	public static final Account COLLECT = new Account() {
		@Override
		public void cpuNode(CPUWithdraw node) {
			add(node.cost());
		}

		@Override
		public void otherNode(IRNode node) {
			// no-op
		}
	};

	private static class Visitor extends DefaultNodeActionVisitor {

		private final Account account;

		public Visitor(Account account) {
			this.account = Check.notNull(account);
		}

		@Override
		protected void action(IRNode node) {
			account.otherNode(node);
		}

		@Override
		public void visit(CPUWithdraw node) {
			account.cpuNode(node);
		}

	}

	public CPUAccountingVisitor(Account acc) {
		super(new Visitor(acc));
		this.acc = Check.notNull(acc);
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
			int cost = acc.get();
			removeCPUNodes(currentBody());
			if (cost > 0) {
				currentBody().add(0, new CPUWithdraw(cost));
			}
		}
		finally {
			acc.reset();
		}
	}

}
