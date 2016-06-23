package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.compiler.BlocksVisitor;
import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.util.Check;

import java.util.HashSet;
import java.util.Set;

// A visitor that checks that each temp is assigned to before used, and that no temp
// is assigned to more than once.
public class TempUseVerifierVisitor extends BlocksVisitor {

	private final Set<Val> assignedTo;
	private final Set<Val> used;

	public TempUseVerifierVisitor() {
		this.assignedTo = new HashSet<>();
		this.used = new HashSet<>();
	}

	private void assign(Val v) {
		Check.notNull(v);
		if (!assignedTo.add(v)) {
			throw new IllegalStateException(v.toString() + " assigned to more than once");
		}
	}

	private void assign(PhiVal v) {
		// TODO
	}

	private void use(Val v) {
		Check.notNull(v);
		if (!assignedTo.contains(v)) {
			throw new IllegalStateException(v.toString() + " used before assigned to");
		}
		used.add(v);
	}

	private void use(PhiVal v) {
		// TODO
	}

	@Override
	public void visit(LoadConst.Nil node) {
		assign(node.dest());
	}

	@Override
	public void visit(LoadConst.Bool node) {
		assign(node.dest());
	}

	@Override
	public void visit(LoadConst.Int node) {
		assign(node.dest());
	}

	@Override
	public void visit(LoadConst.Flt node) {
		assign(node.dest());
	}

	@Override
	public void visit(LoadConst.Str node) {
		assign(node.dest());
	}

	@Override
	public void visit(BinOp node) {
		use(node.left());
		use(node.right());
		assign(node.dest());
	}

	@Override
	public void visit(UnOp node) {
		use(node.arg());
		assign(node.dest());
	}

	@Override
	public void visit(TabNew node) {
		assign(node.dest());
	}

	@Override
	public void visit(TabGet node) {
		use(node.obj());
		use(node.key());
		assign(node.dest());
	}

	@Override
	public void visit(TabSet node) {
		use(node.dest());
		use(node.key());
		use(node.value());
	}

	@Override
	public void visit(TabStackAppend node) {
		use(node.dest());
	}

	@Override
	public void visit(VarLoad node) {
		assign(node.dest());
	}

	@Override
	public void visit(VarStore node) {
		use(node.src());
	}

	@Override
	public void visit(UpLoad node) {
		assign(node.dest());
	}

	@Override
	public void visit(UpStore node) {
		use(node.src());
	}

	@Override
	public void visit(Vararg node) {
		// no effect on temps
	}

	private void useVList(VList vl) {
		Check.notNull(vl);
		for (Val t : vl.addrs()) {
			use(t);
		}
	}

	@Override
	public void visit(Ret node) {
		useVList(node.args());
	}

	@Override
	public void visit(TCall node) {
		use(node.target());
		useVList(node.args());
	}

	@Override
	public void visit(Call node) {
		use(node.fn());
		useVList(node.args());
	}

	@Override
	public void visit(StackGet node) {
		assign(node.dest());
	}

	@Override
	public void visit(PhiStore node) {
		use(node.src());
		assign(node.dest());
	}

	@Override
	public void visit(PhiLoad node) {
		use(node.src());
		assign(node.dest());
	}

	@Override
	public void visit(Label node) {
		// no effect on temps
	}

	@Override
	public void visit(Jmp node) {
		// no effect on temps
	}

	@Override
	public void visit(Closure node) {
		assign(node.dest());
	}

	@Override
	public void visit(ToNumber node) {
		use(node.src());
		assign(node.dest());
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

	@Override
	public void visit(ToNext node) {
		// no effect on temps
	}

}
