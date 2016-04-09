package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.ClassEmitter;
import net.sandius.rembulan.compiler.gen.block.CodeEmitter;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;

public class JavaBytecodeCodeVisitor extends CodeVisitor {

	private final CodeEmitter e;

	public JavaBytecodeCodeVisitor(CodeEmitter e) {
		this.e = Check.notNull(e);
	}

	@Override
	public void visitMove(Object id, SlotState st, int r_src, int r_dest) {
		e._load_reg(r_src, st);
		e._store(r_dest, st);
	}

	@Override
	public void visitLoadK(Object id, SlotState st, int r_dest, int constIndex) {
		e._load_k(constIndex);
		e._store(r_dest, st);
	}

	@Override
	public void visitLoadBool(Object id, SlotState st, int r_dest, boolean value) {
		e._load_constant(value);
		e._store(r_dest, st);
	}

	@Override
	public void visitLoadNil(Object id, SlotState st, int r_dest, int count) {
		for (int i = 0; i < count; i++) {
			e._push_null();
			e._store(r_dest + i, st);
		}
	}

	@Override
	public void visitGetUpVal(Object id, SlotState st, int r_dest, int upvalueIndex) {
		e._get_upvalue_ref(upvalueIndex);
		e._get_upvalue_value();
		e._store(r_dest, st);
	}

	@Override
	public void visitGetTabUp(Object id, SlotState st, int r_dest, int upvalueIndex, int rk_key) {
		e._save_pc(id);

		e._loadState();
		e._loadObjectSink();
		e._get_upvalue_ref(upvalueIndex);
		e._get_upvalue_value();
		e._load_reg_or_const(rk_key, st);
		e._dispatch_index();

		e._resumptionPoint(id);
		e._retrieve_0();
		e._store(r_dest, st);
	}

	@Override
	public void visitGetTable(Object id, SlotState st, int r_dest, int r_tab, int rk_key) {
		e._save_pc(id);

		e._loadState();
		e._loadObjectSink();
		e._load_reg(r_tab, st);
		e._load_reg_or_const(rk_key, st);
		e._dispatch_index();

		e._resumptionPoint(id);
		e._retrieve_0();
		e._store(r_dest, st);
	}

	@Override
	public void visitSetTabUp(Object id, SlotState st, int upvalueIndex, int rk_key, int rk_value) {
		e._save_pc(id);

		e._loadState();
		e._loadObjectSink();
		e._get_upvalue_ref(upvalueIndex);
		e._get_upvalue_value();
		e._load_reg_or_const(rk_key, st);
		e._load_reg_or_const(rk_value, st);
		e._dispatch_newindex();

		e._resumptionPoint(id);
	}

	@Override
	public void visitSetUpVal(Object id, SlotState st, int r_src, int upvalueIndex) {
		e._get_upvalue_ref(upvalueIndex);
		e._load_reg(r_src, st);
		e._set_upvalue_value();
	}

	@Override
	public void visitSetTable(Object id, SlotState st, int r_tab, int rk_key, int rk_value) {
		e._save_pc(id);

		e._loadState();
		e._loadObjectSink();
		e._load_reg(r_tab, st);
		e._load_reg_or_const(rk_key, st);
		e._load_reg_or_const(rk_value, st);
		e._dispatch_newindex();

		e._resumptionPoint(id);
	}

	@Override
	public void visitNewTable(Object id, SlotState st, int r_dest, int arraySize, int hashSize) {
		e._new_table(arraySize, hashSize);
		e._store(r_dest, st);
	}

	@Override
	public void visitSelf(Object id, SlotState st, int r_dest, int r_self, int rk_key) {
		e._save_pc(id);

		e._loadState();
		e._loadObjectSink();

		e._load_reg(r_self, st);
		e._dup();
		e._store(r_dest + 1, st);

		e._load_reg_or_const(rk_key, st);
		e._dispatch_index();

		e._resumptionPoint(id);
		e._retrieve_0();
		e._store(r_dest, st);
	}

	@Override
	public void visitConcat(Object id, SlotState st, int r_dest, int r_begin, int r_end) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void visitEq(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		e._cmp("eq", rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity);
	}

	@Override
	public void visitLe(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		e._cmp("le", rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity);
	}

	@Override
	public void visitLt(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		e._cmp("lt", rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity);
	}

	@Override
	public void visitTest(Object id, SlotState st, int r_index, boolean value, Object trueBranchIdentity, Object falseBranchIdentity) {
		Type tpe = st.typeAt(r_index);

		if (tpe.isSubtypeOf(LuaTypes.BOOLEAN)) {
			throw new UnsupportedOperationException();  // TODO
		}
		else if (tpe.equals(LuaTypes.ANY) || tpe.equals(LuaTypes.DYNAMIC)) {
			// TODO: check correctness for DYNAMIC

			e._load_reg(r_index, st);

			if (value)  {
				e._if_null(falseBranchIdentity);
				e._next_insn(trueBranchIdentity);
			}
			else {
				e._if_nonnull(falseBranchIdentity);
				e._next_insn(trueBranchIdentity);
			}

		}
		else if (tpe.equals(LuaTypes.NIL)) {
			// TODO: should be inlined
			e._goto(falseBranchIdentity);
		}
		else {
			// TODO: should be inlined
			e._goto(trueBranchIdentity);
		}

	}

	@Override
	public void visitCall(Object id, SlotState st, int r_tgt, int b, int c) {
		e._save_pc(id);

		if (b > 0) {
			int kind = ClassEmitter.kind(b - 1,  false);

			e._loadState();
			e._loadObjectSink();
			e._load_reg(r_tgt, st);

			if (kind < 0) {
				// need to pack args into an array
				e._pack_regs(r_tgt + 1, st, b - 1);
			}
			else {
				// pass args through the stack
				e._load_regs(r_tgt + 1, st, kind);
			}

			e._dispatch_call(kind);
		}
		else {
			// TODO
			throw new UnsupportedOperationException("CALL with b == 0");
		}

		e._resumptionPoint(id);

		if (c > 0) {
			e._retrieve_and_store_n(c - 1, r_tgt, st);
		}
		else {
			// TODO
			throw new UnsupportedOperationException("CALL with c == 0");
		}
	}

	@Override
	public void visitTailCall(Object id, SlotState st, int r_tgt, int b) {
		if (b > 0) {
			// b - 1 is the actual number of arguments to the tailcall
			e._loadObjectSink();
			e._load_regs(r_tgt, st, b);  // target is at r_tgt, plus (b - 1) arguments
			e._tailcall(b - 1);
			e._return();
		}
		else {
			e._missing(id);
		}
	}

	@Override
	public void visitReturn(Object id, SlotState st, int r_from, int b) {
		if (b > 0) {
			// b - 1 is the actual number of results
			e._loadObjectSink();
			e._load_regs(r_from, st, b - 1);
			e._setret(b - 1);
			e._return();
		}
		else {
			// TODO
			e._missing(id);
		}
	}

	@Override
	public void visitForLoop(Object id, SlotState st, int r_base) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void visitForPrep(Object id, SlotState st, int r_base) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void visitClosure(Object id, SlotState st, int r_dest, int index) {
		String closureClassName = e.context().nestedPrototypeName(index);

		ReadOnlyArray<Prototype.UpvalueDesc> uvds = e.context().nestedPrototype(index).getUpValueDescriptions();

		for (Prototype.UpvalueDesc uvd : uvds) {
			if (uvd.inStack && !st.isCaptured(uvd.index)) {
				e._capture(uvd.index);
				st = st.capture(uvd.index);  // just marking it so that we can store properly
			}
		}

		e._new(closureClassName);
		e._dup();

		for (Prototype.UpvalueDesc uvd : uvds) {
			if (uvd.inStack) {
				// by id point all upvalues have been captured
				e._load_reg_value(uvd.index, Upvalue.class);
			}
			else {
				e._get_upvalue_ref(uvd.index);
			}
		}

		e._closure_ctor(closureClassName, uvds.size());

		e._store(r_dest, st);
	}

	@Override
	public void visitVararg(Object id, SlotState st, int r_base, int b) {
		throw new UnsupportedOperationException();  // TODO
	}

}
