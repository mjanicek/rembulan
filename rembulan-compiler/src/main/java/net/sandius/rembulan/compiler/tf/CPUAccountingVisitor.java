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
