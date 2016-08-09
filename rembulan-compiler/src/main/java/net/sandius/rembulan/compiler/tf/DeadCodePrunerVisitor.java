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

import net.sandius.rembulan.compiler.analysis.LivenessInfo;
import net.sandius.rembulan.compiler.analysis.TypeInfo;
import net.sandius.rembulan.compiler.ir.AbstractVal;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.LoadConst;
import net.sandius.rembulan.compiler.ir.Var;
import net.sandius.rembulan.compiler.ir.VarInit;
import net.sandius.rembulan.compiler.ir.VarLoad;
import net.sandius.rembulan.compiler.ir.VarStore;
import net.sandius.rembulan.util.Check;

public class DeadCodePrunerVisitor extends CodeTransformerVisitor {

	private final TypeInfo types;
	private final LivenessInfo liveness;

	public DeadCodePrunerVisitor(TypeInfo types, LivenessInfo liveness) {
		this.types = Check.notNull(types);
		this.liveness = Check.notNull(liveness);
	}

	protected void skip(BodyNode node) {
		int idx = currentBody().indexOf(node);
		if (idx < 0) {
			throw new IllegalStateException("Node not found: " + node);
		}
		else {
			currentBody().remove(idx);
		}
	}

	protected boolean isLiveOut(IRNode at, AbstractVal v) {
		return liveness.entry(at).outVal().contains(v);
	}

	protected boolean isLiveOut(IRNode at, Var v) {
		return liveness.entry(at).outVar().contains(v);
	}

	@Override
	public void visit(LoadConst.Bool node) {
		if (!isLiveOut(node, node.dest())) {
			skip(node);
		}
	}

	@Override
	public void visit(LoadConst.Int node) {
		if (!isLiveOut(node, node.dest())) {
			skip(node);
		}
	}

	@Override
	public void visit(LoadConst.Flt node) {
		if (!isLiveOut(node, node.dest())) {
			skip(node);
		}
	}

	@Override
	public void visit(LoadConst.Str node) {
		if (!isLiveOut(node, node.dest())) {
			skip(node);
		}
	}

	@Override
	public void visit(VarInit node) {
		// FIXME: very inefficient
		if (!types.isReified(node.var()) && !isLiveOut(node, node.var())) {
			skip(node);
		}
	}

	@Override
	public void visit(VarStore node) {
		// FIXME: very inefficient
		if (!types.isReified(node.var()) && !isLiveOut(node, node.var())) {
			skip(node);
		}
	}

	@Override
	public void visit(VarLoad node) {
		// FIXME: very inefficient
		if (!types.isReified(node.var()) && !isLiveOut(node, node.dest())) {
			skip(node);
		}
	}

}
