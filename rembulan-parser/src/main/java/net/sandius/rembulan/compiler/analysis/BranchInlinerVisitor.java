package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.ir.BlockTermNode;
import net.sandius.rembulan.compiler.ir.Branch;
import net.sandius.rembulan.compiler.ir.IRVisitor;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.util.Check;

public class BranchInlinerVisitor extends IRVisitor {

	private final TypeInfo types;
	private final InlineHandler handler;

	private Boolean inline;
	private BlockTermNode result;

	public static interface InlineHandler {

		void noInline(Branch b);

		void inlineAsTrue(Branch b);

		void inlineAsFalse(Branch b);

	}

	public BranchInlinerVisitor(TypeInfo types, InlineHandler handler) {
		this.types = Check.notNull(types);
		this.handler = Check.notNull(handler);
	}

	@Override
	public void visit(Branch branch) {
		try {
			inline = null;
			super.visit(branch);
			if (inline != null) {
				if (inline) {
					handler.inlineAsTrue(branch);
				}
				else {
					handler.inlineAsFalse(branch);
				}
			}
			else {
				handler.noInline(branch);
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
		else if (t.isSubtypeOf(LuaTypes.ANY)) {
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
		else if (t.isSubtypeOf(LuaTypes.ANY) && !t.isSubtypeOf(LuaTypes.BOOLEAN)) {
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
