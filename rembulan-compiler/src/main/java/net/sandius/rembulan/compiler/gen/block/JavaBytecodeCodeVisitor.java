package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.CodeVisitor;
import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntIterable;
import net.sandius.rembulan.util.IntIterator;
import net.sandius.rembulan.util.ReadOnlyArray;

public class JavaBytecodeCodeVisitor extends CodeVisitor {

	private final CodeEmitter e;

	public JavaBytecodeCodeVisitor(CodeEmitter e) {
		this.e = Check.notNull(e);
	}

	@Override
	public void visitTarget(Object id) {
		e._label_here(id);
	}

	@Override
	public void visitJump(Object id, Object target) {
		e._goto(target);
	}

	@Override
	public void visitCapture(Object it, SlotState st, IntIterable indices) {
		IntIterator iit = indices.iterator();
		while (iit.hasNext()) {
			int idx = iit.next();
			e._capture(idx);
		}
	}

	@Override
	public void visitCloseUpvalues(Object it, SlotState st, int fromIndex) {
		for (int i = fromIndex; i < st.size(); i++) {
			if (st.isCaptured(i)) {
				e._uncapture(i);
			}
		}
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

	private void binaryOperation(Object id, String method, StaticMathImplementation staticMath, SlotState s, int r_dest, int rk_left, int rk_right) {
		LuaInstruction.NumOpType ot = staticMath.opType(
				LuaBinaryOperation.slotType(e.context(), s, rk_left),
				LuaBinaryOperation.slotType(e.context(), s, rk_right));

		switch (ot) {
			case Integer:
				e._load_reg_or_const(rk_left, s, Number.class);
				e._load_reg_or_const(rk_right, s, Number.class);
				e._dispatch_binop(method + "_integer", Number.class);
				e._store(r_dest, s);
				break;

			case Float:
				e._load_reg_or_const(rk_left, s, Number.class);
				e._load_reg_or_const(rk_right, s, Number.class);
				e._dispatch_binop(method + "_float", Number.class);
				e._store(r_dest, s);
				break;

			case Number:
				e._load_reg_or_const(rk_left, s, Number.class);
				e._load_reg_or_const(rk_right, s, Number.class);
				e._dispatch_binop(method, Number.class);
				e._store(r_dest, s);
				break;

			case Any:
				e._save_pc(id);

				e._loadState();
				e._loadObjectSink();
				e._load_reg_or_const(rk_left, s, Object.class);
				e._load_reg_or_const(rk_right, s, Object.class);
				e._dispatch_generic_mt_2(method);

				e._resumptionPoint(id);
				e._retrieve_0();
				e._store(r_dest, s);
				break;
		}
	}

	@Override
	public void visitAdd(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "add", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.ADD), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitSub(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "sub", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.SUB), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitMul(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "mul", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.MUL), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitMod(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "mod", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.MOD), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitPow(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "pow", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.POW), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitDiv(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "div", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.DIV), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitIDiv(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "idiv", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.IDIV), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitBAnd(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "band", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.BAND), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitBOr(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "bor", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.BOR), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitBXOr(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "bxor", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.BXOR), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitShl(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "shl", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.SHL), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitShr(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		binaryOperation(id, "shr", LuaBinaryOperation.mathForOp(LuaBinaryOperation.Op.SHR), st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitUnm(Object id, SlotState st, int r_dest, int r_arg) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void visitBNot(Object id, SlotState st, int r_dest, int r_arg) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void visitNot(Object id, SlotState st, int r_dest, int r_arg) {
		e._not(r_arg, r_dest, st);
	}

	@Override
	public void visitLen(Object id, SlotState st, int r_dest, int r_arg) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void visitConcat(Object id, SlotState st, int r_dest, int r_begin, int r_end) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void visitEq(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		e._cmp(id, "eq", rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity);
	}

	@Override
	public void visitLe(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		e._cmp(id, "le", rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity);
	}

	@Override
	public void visitLt(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		e._cmp(id, "lt", rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity);
	}

	@Override
	public void visitTest(Object id, SlotState st, int r_index, boolean value, Object trueBranchIdentity, Object falseBranchIdentity) {
		Type tpe = st.typeAt(r_index);

		e._load_reg(r_index, st);

		if (tpe.isSubtypeOf(LuaTypes.BOOLEAN)) {
			e._unbox_boolean();
		}
		else {
			e._to_boolean();
		}

		if (value)  {
			e._ifzero(trueBranchIdentity);
			e._next_insn(falseBranchIdentity);

//			e._ifzero(falseBranchIdentity);
//			e._next_insn(trueBranchIdentity);
		}
		else {
			e._ifnonzero(falseBranchIdentity);
			e._next_insn(trueBranchIdentity);
		}

//		e._next_insn(trueBranchIdentity);
	}

	@Override
	public void visitCall(Object id, SlotState st, int r_tgt, int b, int c) {
		e._save_pc(id);

		if (b > 0) {
			int kind = InvokeKind.encode(b - 1,  false);

			e._loadState();
			e._loadObjectSink();
			e._load_reg(r_tgt, st);

			if (kind == 0) {
				// pack args into an array
				e._pack_regs(r_tgt + 1, st, b - 1);
			}
			else {
				// pass (kind - 1) args through the stack
				e._load_regs(r_tgt + 1, st, kind - 1);
			}

			e._dispatch_call(kind);
		}
		else {
			Check.isTrue(st.hasVarargs());

			int n = st.varargPosition() - (r_tgt + 1);

			e._loadState();
			e._loadObjectSink();
			e._load_reg(r_tgt, st);

			if (n == 0) {
				// just take the varargs
				e._load_object_sink_as_array();
			}
			else if (n < 0) {
				// drop n elements from the object sink
				e._drop_from_object_sink(-n);
				e._load_object_sink_as_array();
			}
			else {
				// prepend -n elements
				e._pack_regs(r_tgt + 1, st, n);
				e._load_object_sink_as_array();
				e._concat_arrays();
			}

			e._dispatch_call(0);
		}

		e._resumptionPoint(id);

		if (c > 0) {
			st = st.consumeVarargs();
			e._retrieve_and_store_n(c - 1, r_tgt, st);
		}
		else {
			// keep results in the object sink
			// TODO: should all registers from r_tgt onwards be cleared?
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
			e._tailcall_vararg(r_tgt, st);
			e._return();
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
			e._setret_vararg(r_from, st);
			e._return();
		}
	}

	@Override
	public void visitForLoop(Object id, SlotState st, int r_base, Object trueBranch, Object falseBranch) {
		e._forloop(st, r_base, trueBranch, falseBranch);
	}

	@Override
	public void visitForPrep(Object id, SlotState st, int r_base) {
		e._forprep(st, r_base);
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
		if (b > 0) {
			// determinate case: (b - 1) is the number of varargs to load

			int n = b - 1;

			if (n > 0) {
				e._push_varargs();
				for (int i = 0; i < n; i++) {
					if (i + 1 < n) {
						e._dup();
					}
					e._load_vararg(i);
					e._store(r_base + i, st);
				}
			}
		}
		else {
			// indeterminate case
			e._loadObjectSink();
			e._push_varargs();
			e._save_array_to_object_sink();
		}
	}

}
