package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.util.Check;

import java.util.HashSet;
import java.util.Set;

// A visitor that checks that each temp is assigned to before used, and that no temp
// is assigned to more than once.
public class TempUseVerifierVisitor extends IRVisitor {

	private final Set<Temp> assignedTo;
	private final Set<Temp> used;

	public TempUseVerifierVisitor() {
		this.assignedTo = new HashSet<>();
		this.used = new HashSet<>();
	}

	private void assign(Temp t) {
		Check.notNull(t);
		if (!assignedTo.add(t)) {
			throw new IllegalStateException(t.toString() + " assigned to more than once");
		}
	}

	private void use(Temp t) {
		Check.notNull(t);
		if (!assignedTo.contains(t)) {
			throw new IllegalStateException(t.toString() + " used before assigned to");
		}
		used.add(t);
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
		for (Temp t : vl.addrs()) {
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
	public void visit(Mov node) {
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
	public void visit(CJmp node) {
		use(node.addr());
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
	public void visit(CheckForEnd node) {
		use(node.var());
		use(node.limit());
		use(node.step());
	}

	@Override
	public void visit(JmpIfNil node) {
		use(node.addr());
	}

	@Override
	public void visit(ToNext node) {
		// no effect on temps
	}

}
