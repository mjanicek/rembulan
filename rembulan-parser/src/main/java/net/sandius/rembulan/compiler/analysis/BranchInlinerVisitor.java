package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.ir.Branch;
import net.sandius.rembulan.compiler.ir.Jmp;
import net.sandius.rembulan.compiler.ir.ToNext;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.util.Check;

public class BranchInlinerVisitor extends BlockTransformerVisitor {

	private final TypeInfo types;
	private Boolean inline;

	public BranchInlinerVisitor(TypeInfo types) {
		this.types = Check.notNull(types);
	}

	@Override
	public void visit(Branch branch) {
		try {
			inline = null;
			branch.condition().accept(this);
			if (inline != null) {
				if (inline) {
					setEnd(new ToNext(branch.next()));
				}
				else {
					setEnd(new Jmp(branch.jmpDest()));
				}
			}
		}
		finally {
			inline = null;
		}
	}

	@Override
	public void visit(Branch.Condition.Nil cond) {
		Type t = types.typeOf(cond.addr());
		if (t.isSubtypeOf(LuaTypes.NIL)) {
			inline = Boolean.TRUE;
		}
		else if (t.isSubtypeOf(LuaTypes.ANY) && !t.equals(LuaTypes.ANY)) {
			inline = Boolean.FALSE;
		}
		else {
			inline = null;
		}
	}

	@Override
	public void visit(Branch.Condition.Bool cond) {
		Type t = types.typeOf(cond.addr());
		if (t.isSubtypeOf(LuaTypes.NIL)) {
			// t evaluates to false
			inline = !cond.expected();
		}
		else if (t.isSubtypeOf(LuaTypes.ANY) && !t.equals(LuaTypes.ANY) && !t.isSubtypeOf(LuaTypes.BOOLEAN)) {
			// t evaluates to true
			inline = cond.expected();
		}
		else {
			inline = null;
		}
	}

	@Override
	public void visit(Branch.Condition.NumLoopEnd cond) {
		inline = null;
	}

}
