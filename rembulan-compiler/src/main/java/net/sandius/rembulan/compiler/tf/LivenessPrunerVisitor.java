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

public class LivenessPrunerVisitor extends CodeTransformerVisitor {

	private final TypeInfo types;
	private final LivenessInfo liveness;

	public LivenessPrunerVisitor(TypeInfo types, LivenessInfo liveness) {
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
