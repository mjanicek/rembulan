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

package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.ir.*;

abstract class AbstractUseDefVisitor extends IRVisitor {

	protected abstract void def(Val v);
	protected abstract void use(Val v);
		
	protected abstract void def(PhiVal pv);
	protected abstract void use(PhiVal pv);

	protected abstract void def(MultiVal mv);
	protected abstract void use(MultiVal mv);

	protected abstract void def(Var v);
	protected abstract void use(Var v);

	protected abstract void def(UpVar uv);
	protected abstract void use(UpVar uv);

	@Override
	public void visit(LoadConst.Nil node) {
		def(node.dest());
	}

	@Override
	public void visit(LoadConst.Bool node) {
		def(node.dest());
	}

	@Override
	public void visit(LoadConst.Int node) {
		def(node.dest());
	}

	@Override
	public void visit(LoadConst.Flt node) {
		def(node.dest());
	}

	@Override
	public void visit(LoadConst.Str node) {
		def(node.dest());
	}

	@Override
	public void visit(BinOp node) {
		use(node.left());
		use(node.right());
		def(node.dest());
	}

	@Override
	public void visit(UnOp node) {
		use(node.arg());
		def(node.dest());
	}

	@Override
	public void visit(TabNew node) {
		def(node.dest());
	}

	@Override
	public void visit(TabGet node) {
		use(node.obj());
		use(node.key());
		def(node.dest());
	}

	@Override
	public void visit(TabSet node) {
		use(node.obj());
		use(node.key());
		use(node.value());
	}

	@Override
	public void visit(TabRawSet node) {
		use(node.obj());
		use(node.key());
		use(node.value());
	}

	@Override
	public void visit(TabRawSetInt node) {
		use(node.obj());
		use(node.value());
	}

	@Override
	public void visit(TabRawAppendMulti node) {
		use(node.obj());
		use(node.src());
	}

	@Override
	public void visit(VarInit node) {
		use(node.src());
		def(node.var());
	}

	@Override
	public void visit(VarLoad node) {
		use(node.var());
		def(node.dest());
	}

	@Override
	public void visit(VarStore node) {
		use(node.src());
		def(node.var());
	}

	@Override
	public void visit(UpLoad node) {
		use(node.upval());
		def(node.dest());
	}

	@Override
	public void visit(UpStore node) {
		use(node.src());
		def(node.upval());
	}

	@Override
	public void visit(Vararg node) {
		def(node.dest());
	}

	protected void use(VList vlist) {
		for (Val v : vlist.addrs()) {
			use(v);
		}
		if (vlist.suffix() != null) {
			use(vlist.suffix());
		}
	}

	@Override
	public void visit(Ret node) {
		use(node.args());
	}

	@Override
	public void visit(TCall node) {
		use(node.target());
		use(node.args());
	}

	@Override
	public void visit(Call node) {
		use(node.fn());
		use(node.args());
		def(node.dest());
	}

	@Override
	public void visit(MultiGet node) {
		use(node.src());
		def(node.dest());
	}

	@Override
	public void visit(PhiStore node) {
		use(node.src());
		def(node.dest());
	}

	@Override
	public void visit(PhiLoad node) {
		use(node.src());
		def(node.dest());
	}

	@Override
	public void visit(Label node) {
		// no effect
	}

	@Override
	public void visit(Jmp node) {
		// no effect
	}

	@Override
	public void visit(Closure node) {
		for (AbstractVar v : node.args()) {
			if (v instanceof Var) {
				use((Var) v);
			}
			else if (v instanceof UpVar) {
				use((UpVar) v);
			}
			else {
				throw new IllegalStateException("Illegal abstract var: " + v);
			}
		}
		def(node.dest());
	}

	@Override
	public void visit(ToNumber node) {
		use(node.src());
		def(node.dest());
	}

	@Override
	public void visit(ToNext node) {
		// no effect
	}

	@Override
	public void visit(Branch branch) {
		branch.condition().accept(this);
	}

	@Override
	public void visit(Branch.Condition.Nil cond) {
		use(cond.addr());
	}

	@Override
	public void visit(Branch.Condition.Bool cond) {
		use(cond.addr());
	}

	@Override
	public void visit(Branch.Condition.NumLoopEnd cond) {
		use(cond.var());
		use(cond.limit());
		use(cond.step());
	}

}
