package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.BasicBlock;
import net.sandius.rembulan.compiler.Blocks;
import net.sandius.rembulan.compiler.BlocksVisitor;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.compiler.ir.PhiVal;
import net.sandius.rembulan.compiler.ir.UpVar;
import net.sandius.rembulan.compiler.ir.Val;
import net.sandius.rembulan.compiler.ir.Var;

import java.util.Map;
import java.util.Stack;

public class SlotAssignmentVisitor extends BlocksVisitor {

	private boolean changed;

	public SlotAssignmentVisitor() {
		// TODO
	}

	private static class UseDefVisitor extends AbstractUseDefVisitor {

		public UseDefVisitor() {
			// TODO
		}

		@Override
		protected void def(Val v) {
			throw new UnsupportedOperationException();  // TODO
		}

		@Override
		protected void use(Val v) {
			throw new UnsupportedOperationException();  // TODO
		}

		@Override
		protected void def(PhiVal pv) {
			throw new UnsupportedOperationException();  // TODO
		}

		@Override
		protected void use(PhiVal pv) {
			throw new UnsupportedOperationException();  // TODO
		}

		@Override
		protected void def(Var v) {
			throw new UnsupportedOperationException();  // TODO
		}

		@Override
		protected void use(Var v) {
			throw new UnsupportedOperationException();  // TODO
		}

		@Override
		protected void def(UpVar uv) {
			throw new UnsupportedOperationException();  // TODO
		}

		@Override
		protected void use(UpVar uv) {
			throw new UnsupportedOperationException();  // TODO
		}

	}

	public SlotAllocInfo result() {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void visit(Blocks blocks) {
		Stack<Label> open = new Stack<>();

		Map<Label, BasicBlock> index = blocks.index();

		while (!open.isEmpty()) {
			Label l = open.pop();
			BasicBlock b = index.get(l);

			assert (b != null);

			changed = false;
			try {
				visit(b);
				if (changed) {
					for (Label nxt : b.end().nextLabels()) {
						open.add(nxt);
					}
				}
			}
			finally {
				changed = false;
			}
		}

	}

	@Override
	public void visit(BasicBlock block) {
		UseDefVisitor udv = new UseDefVisitor();

		for (IRNode n : block.body()) {
			n.accept(udv);
		}
		block.end().accept(udv);

		// TODO: retrieve the result

		// TODO: set the changed flag
	}

}
