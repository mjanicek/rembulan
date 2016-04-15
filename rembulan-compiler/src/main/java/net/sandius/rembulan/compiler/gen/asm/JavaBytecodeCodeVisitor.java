package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.compiler.gen.CodeVisitor;
import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.gen.block.LuaBinaryOperation;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntIterable;
import net.sandius.rembulan.util.IntIterator;
import net.sandius.rembulan.util.ReadOnlyArray;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;

public class JavaBytecodeCodeVisitor extends CodeVisitor {

	private final CodeEmitter e;

	public JavaBytecodeCodeVisitor(CodeEmitter e) {
		this.e = Check.notNull(e);
	}

	protected void add(InsnList il) {
		e.code().add(il);
	}

	protected void add(AbstractInsnNode il) {
		e.code().add(il);
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
			add(e.captureRegister(idx));
		}
	}

	@Override
	public void visitCloseUpvalues(Object it, SlotState st, int fromIndex) {
		for (int i = fromIndex; i < st.size(); i++) {
			if (st.isCaptured(i)) {
				add(e.uncaptureRegister(i));
			}
		}
	}

	@Override
	public void visitMove(Object id, SlotState st, int r_src, int r_dest) {
		add(e.loadRegister(r_src, st));
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitLoadK(Object id, SlotState st, int r_dest, int constIndex) {
		add(e.loadConstant(constIndex));
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitLoadBool(Object id, SlotState st, int r_dest, boolean value) {
		add(ASMUtils.loadBoxedBoolean(value));
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitLoadNil(Object id, SlotState st, int r_dest, int count) {
		for (int i = 0; i < count; i++) {
			add(CodeEmitter.loadNull());
			add(e.storeToRegister(r_dest + i, st));
		}
	}

	@Override
	public void visitGetUpVal(Object id, SlotState st, int r_dest, int upvalueIndex) {
		add(e.getUpvalueReference(upvalueIndex));
		add(e.getUpvalueValue());
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitGetTabUp(Object id, SlotState st, int r_dest, int upvalueIndex, int rk_key) {
		e._save_pc(id);

		add(e.loadDispatchPreamble());
		add(e.getUpvalueReference(upvalueIndex));
		add(e.getUpvalueValue());
		add(e.loadRegisterOrConstant(rk_key, st));
		add(e.dispatchIndex());

		e._resumptionPoint(id);
		add(e.retrieve_0());
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitGetTable(Object id, SlotState st, int r_dest, int r_tab, int rk_key) {
		e._save_pc(id);

		add(e.loadDispatchPreamble());
		add(e.loadRegister(r_tab, st));
		add(e.loadRegisterOrConstant(rk_key, st));
		add(e.dispatchIndex());

		e._resumptionPoint(id);
		add(e.retrieve_0());
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitSetTabUp(Object id, SlotState st, int upvalueIndex, int rk_key, int rk_value) {
		e._save_pc(id);

		add(e.loadDispatchPreamble());
		add(e.getUpvalueReference(upvalueIndex));
		add(e.getUpvalueValue());
		add(e.loadRegisterOrConstant(rk_key, st));
		add(e.loadRegisterOrConstant(rk_value, st));
		add(e.dispatchNewindex());

		e._resumptionPoint(id);
	}

	@Override
	public void visitSetUpVal(Object id, SlotState st, int r_src, int upvalueIndex) {
		add(e.getUpvalueReference(upvalueIndex));
		add(e.loadRegister(r_src, st));
		add(e.setUpvalueValue());
	}

	@Override
	public void visitSetTable(Object id, SlotState st, int r_tab, int rk_key, int rk_value) {
		e._save_pc(id);

		add(e.loadDispatchPreamble());
		add(e.loadRegister(r_tab, st));
		add(e.loadRegisterOrConstant(rk_key, st));
		add(e.loadRegisterOrConstant(rk_value, st));
		add(e.dispatchNewindex());

		e._resumptionPoint(id);
	}

	@Override
	public void visitNewTable(Object id, SlotState st, int r_dest, int arraySize, int hashSize) {
		add(e.loadLuaState());
		add(CodeEmitter.LuaState_prx.newTable(arraySize, hashSize));
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitSelf(Object id, SlotState st, int r_dest, int r_self, int rk_key) {
		e._save_pc(id);

		add(e.loadDispatchPreamble());

		add(e.loadRegister(r_self, st));
		add(new InsnNode(DUP));
		add(e.storeToRegister(r_dest + 1, st));

		add(e.loadRegisterOrConstant(rk_key, st));
		add(e.dispatchIndex());

		e._resumptionPoint(id);
		add(e.retrieve_0());
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitAdd(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.ADD, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitSub(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.SUB, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitMul(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.MUL, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitMod(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.MOD, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitPow(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.POW, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitDiv(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.DIV, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitIDiv(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.IDIV, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitBAnd(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.BAND, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitBOr(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.BOR, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitBXOr(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.BXOR, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitShl(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.SHL, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitShr(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		e.binaryOperation(LuaBinaryOperation.Op.SHR, st, r_dest, rk_left, rk_right);
	}

	@Override
	public void visitUnm(Object id, SlotState st, int r_dest, int r_arg) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void visitBNot(Object id, SlotState st, int r_dest, int r_arg) {
		e._bnot(id, r_arg, r_dest, st);
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

		if (st.typeAt(r_index).isSubtypeOf(LuaTypes.BOOLEAN)) {
			add(e.loadRegister(r_index, st, Boolean.class));
			add(CodeEmitter.booleanValue());
		}
		else {
			add(e.loadRegister(r_index, st));
			add(CodeEmitter.objectToBoolean());
		}

		if (value)  {
			// expected to be true, i.e. non-zero
			e._ifzero(falseBranchIdentity);
		}
		else {
			// expected to be false, i.e. zero
			e._ifnonzero(falseBranchIdentity);
		}

		e._next_insn(trueBranchIdentity);
	}

	@Override
	public void visitCall(Object id, SlotState st, int r_tgt, int b, int c) {
		e._save_pc(id);

		int actualKind = InvokeKind.fromLua(b);

		add(e.loadDispatchPreamble());
		add(e.loadRegister(r_tgt, st));
		add(e.mapInvokeArgumentsToKinds(r_tgt + 1, st, b, actualKind));
		add(e.dispatchCall(actualKind));

		e._resumptionPoint(id);

		if (c > 0) {
			st = st.consumeVarargs();
			add(e.retrieveAndStore(r_tgt, st, c - 1));
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
			add(e.loadObjectSink());
			// FIXME: this needs to be remapped to an available invoke kind
			add(e.loadRegisters(r_tgt, st, b));  // target is at r_tgt, plus (b - 1) arguments
			add(CodeEmitter.ObjectSink_prx.tailCall(b - 1));
			add(new InsnNode(RETURN));
		}
		else {
			add(e.setReturnValuesUpToStackTop(r_tgt, st));
			add(e.loadObjectSink());
			add(CodeEmitter.ObjectSink_prx.markAsTailCall());
			add(new InsnNode(RETURN));
		}
	}

	@Override
	public void visitReturn(Object id, SlotState st, int r_from, int b) {
		if (b > 0) {
			// b - 1 is the actual number of results
			add(e.loadObjectSink());
			add(e.setReturnValuesFromRegisters(r_from, st, b - 1));
			add(new InsnNode(RETURN));
		}
		else {
			add(e.setReturnValuesUpToStackTop(r_from, st));
			add(new InsnNode(RETURN));
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
				add(e.captureRegister(uvd.index));
				st = st.capture(uvd.index);  // just marking it so that we can store properly
			}
		}

		Type closureType = ASMUtils.typeForClassName(closureClassName);

		add(new TypeInsnNode(NEW, closureType.getInternalName()));
		add(new InsnNode(DUP));

		for (Prototype.UpvalueDesc uvd : uvds) {
			if (uvd.inStack) {
				// by this point all upvalues have been captured
				add(e.loadRegisterValue(uvd.index, Upvalue.class));
			}
			else {
				add(e.getUpvalueReference(uvd.index));
			}
		}

		add(ASMUtils.ctor(closureType, e.closureConstructorTypes(uvds.size())));

		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitVararg(Object id, SlotState st, int r_base, int b) {
		if (b > 0) {
			// determinate case: (b - 1) is the number of varargs to load

			int n = b - 1;

			if (n > 0) {
				add(e.loadVarargs());
				for (int i = 0; i < n; i++) {
					if (i + 1 < n) {
						add(new InsnNode(DUP));
					}
					add(CodeEmitter.Util_prx.getArrayElementOrNull(i));
					add(e.storeToRegister(r_base + i, st));
				}
			}
		}
		else {
			// indeterminate case
			add(e.loadObjectSink());
			add(e.loadVarargs());
			add(CodeEmitter.ObjectSink_prx.setToArray());
		}
	}

}
