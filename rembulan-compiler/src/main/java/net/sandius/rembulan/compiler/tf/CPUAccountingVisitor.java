/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.compiler.tf;

import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.compiler.util.DefaultNodeActionVisitor;
import net.sandius.rembulan.util.Check;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

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

		public abstract void noCost();

		public abstract void staticCost(int c);

		public void staticCost() {
			staticCost(1);
		}

		public abstract void dynamicCost();

	}

	public static final Account INITIALISE = new Account() {
		@Override
		public void cpuNode(CPUWithdraw node) {
			// no-op
		}

		@Override
		public void noCost() {
			// no-op
		}

		@Override
		public void staticCost(int c) {
			add(c);
		}

		@Override
		public void dynamicCost() {
			// no-op
		}

	};

	public static final Account COLLECT = new Account() {
		@Override
		public void cpuNode(CPUWithdraw node) {
			add(node.cost());
		}

		@Override
		public void noCost() {
			// no-op
		}

		@Override
		public void staticCost(int c) {
			// no-op
		}

		@Override
		public void dynamicCost() {
			// no-op
		}

	};

	private static class Visitor extends DefaultNodeActionVisitor {

		private final Account account;
		private final Set<Label> visitedLabels;

		public Visitor(Account account) {
			this.account = Objects.requireNonNull(account);
			this.visitedLabels = new HashSet<>();
		}

		// the default action
		@Override
		protected void action(IRNode node) {
			account.noCost();
		}

		@Override
		public void visit(CPUWithdraw node) {
			account.cpuNode(node);
		}

		@Override
		public void visit(Label label) {
			visitedLabels.add(label);
		}

		@Override
		public void visit(BinOp node) {
			account.staticCost();
		}

		@Override
		public void visit(UnOp node) {
			account.staticCost();
		}

		@Override
		public void visit(TabNew node) {
			account.staticCost();
		}

		@Override
		public void visit(TabGet node) {
			account.staticCost();
		}

		@Override
		public void visit(TabSet node) {
			account.staticCost();
		}

		@Override
		public void visit(TabRawSet node) {
			account.staticCost();
		}

		@Override
		public void visit(TabRawSetInt node) {
			account.staticCost();
		}

		@Override
		public void visit(TabRawAppendMulti node) {
			account.dynamicCost();
		}

		@Override
		public void visit(Ret node) {
			account.staticCost();
		}

		@Override
		public void visit(TCall node) {
			account.staticCost();
		}

		@Override
		public void visit(Call node) {
			account.staticCost();
		}

		@Override
		public void visit(Closure node) {
			account.staticCost();
		}

		@Override
		public void visit(ToNumber node) {
			account.staticCost();
		}

		@Override
		public void visit(Branch node) {
			account.staticCost();
		}

		@Override
		public void visit(Jmp node) {
			if (visitedLabels.contains(node.jmpDest())) {
				// count in backward jumps only
				account.staticCost();
			}
		}

	}

	public CPUAccountingVisitor(Account acc) {
		super(new Visitor(acc));
		this.acc = Objects.requireNonNull(acc);
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
