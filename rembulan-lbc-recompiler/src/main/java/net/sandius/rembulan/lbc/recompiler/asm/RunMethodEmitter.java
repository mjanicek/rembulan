package net.sandius.rembulan.lbc.recompiler.asm;

import net.sandius.rembulan.compiler.analysis.types.LuaTypes;
import net.sandius.rembulan.compiler.gen.asm.helpers.ASMUtils;
import net.sandius.rembulan.compiler.gen.asm.helpers.BoxedPrimitivesMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.ConversionMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.LuaStateMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.ObjectSinkMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.UpvalueMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.UtilMethods;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Resumable;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lbc.recompiler.gen.CodeVisitor;
import net.sandius.rembulan.lbc.recompiler.gen.PrototypeContext;
import net.sandius.rembulan.lbc.recompiler.gen.SlotState;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class RunMethodEmitter {

	public final int LV_CONTEXT = 1;
//	public final int LV_STATE = 1;
//	public final int LV_OBJECTSINK = 2;
	public final int LV_RESUME = 2;
	public final int LV_VARARGS = 3;  // index of the varargs argument, if present

	private final ClassEmitter parent;

	private final MethodNode node;

	private final Map<Object, LabelNode> labels;
	private final ArrayList<LabelNode> resumptionPoints;

	private final InsnList resumeSwitch;
	private final InsnList code;
	private final InsnList errorState;
	private final InsnList resumeHandler;

	private final LabelNode l_insns_begin;
	private final LabelNode l_body_begin;
	private final LabelNode l_error_state;
	private final LabelNode l_handler_begin;

	public RunMethodEmitter(ClassEmitter parent) {
		this.parent = Check.notNull(parent);

		this.labels = new HashMap<>();
		this.resumptionPoints = new ArrayList<>();

		this.node = new MethodNode(
				ACC_PRIVATE,
				methodName(),
				methodType().getDescriptor(),
				null,
				exceptions());

		resumeSwitch = new InsnList();
		code = new InsnList();
		errorState = new InsnList();
		resumeHandler = new InsnList();

		l_insns_begin = new LabelNode();
		l_body_begin = new LabelNode();
		l_error_state = new LabelNode();
		l_handler_begin = new LabelNode();

		node.instructions.add(l_insns_begin);

		resumptionPoints.add(l_body_begin);

		code.add(l_body_begin);
		code.add(ASMUtils.frameSame());

		// FIXME: the initial Target emits a label + stack frame immediately after this;
		// that is illegal if there is no instruction in between
		code.add(new InsnNode(NOP));
	}

	protected int registerLocalValueIndex(int index) {
		int offset = parent.isVararg() ? LV_VARARGS + 1 : LV_VARARGS;
		return offset + index;
	}

	public PrototypeContext context() {
		return parent.context();
	}

	public MethodNode node() {
		return node;
	}

	public String methodName() {
		return "run";
	}

	public Type methodType() {
		ArrayList<Type> args = new ArrayList<>();

		args.add(Type.getType(ExecutionContext.class));
		args.add(Type.INT_TYPE);
		if (parent.isVararg()) {
			args.add(ASMUtils.arrayTypeFor(Object.class));
		}
		for (int i = 0; i < numOfRegisters(); i++) {
			args.add(Type.getType(Object.class));
		}
		return Type.getMethodType(Type.VOID_TYPE, args.toArray(new Type[0]));
	}

	public MethodInsnNode methodInvokeInsn() {
		return new MethodInsnNode(
				INVOKESPECIAL,
				parent.thisClassType().getInternalName(),
				methodName(),
				methodType().getDescriptor(),
				false);
	}

	public static String[] exceptions() {
		return new String[] { Type.getInternalName(ControlThrowable.class) };
	}

	protected int numOfRegisters() {
		return parent.context().prototype().getMaximumStackSize();
	}

	protected boolean isResumable() {
		return resumptionPoints.size() > 1;
	}

	public CodeVisitor codeVisitor() {
		return new JavaBytecodeCodeVisitor(this);
	}

	public void end() {
		if (isResumable()) {
			errorState.add(errorState(l_error_state));
			resumeSwitch.add(dispatchTable());
			resumeHandler.add(resumptionHandler(l_handler_begin));

			node.tryCatchBlocks.add(new TryCatchBlockNode(l_insns_begin, l_handler_begin, l_handler_begin, Type.getInternalName(ControlThrowable.class)));
		}

		// local variable declaration

		LabelNode l_insns_end = new LabelNode();

		List<LocalVariableNode> locals = node.localVariables;
		locals.add(new LocalVariableNode("this", parent.thisClassType().getDescriptor(), null, l_insns_begin, l_insns_end, 0));
		locals.add(new LocalVariableNode("context", Type.getDescriptor(ExecutionContext.class), null, l_insns_begin, l_insns_end, LV_CONTEXT));
		locals.add(new LocalVariableNode("rp", Type.INT_TYPE.getDescriptor(), null, l_insns_begin, l_insns_end, LV_RESUME));

		if (parent.isVararg()) {
			locals.add(new LocalVariableNode(
					"varargs",
					ASMUtils.arrayTypeFor(Object.class).getDescriptor(),
					null,
					l_insns_begin,
					l_insns_end,
					LV_VARARGS
					));
		}

		for (int i = 0; i < numOfRegisters(); i++) {
			locals.add(new LocalVariableNode("r_" + i, Type.getDescriptor(Object.class), null, l_insns_begin, l_insns_end, registerLocalValueIndex(i)));
		}

//		if (isResumable()) {
//			locals.add(new LocalVariableNode("ct", Type.getDescriptor(ControlThrowable.class), null, l_handler_begin, l_handler_end, registerOffset() + numOfRegisters()));
//		}

		// TODO: check these
//		node.maxLocals = numOfRegisters() + 4;
//		node.maxStack = numOfRegisters() + 5;

		node.maxLocals = locals.size();
		node.maxStack = 4 + numOfRegisters() + 5;

		node.instructions.add(resumeSwitch);
		node.instructions.add(code);
		node.instructions.add(errorState);
		node.instructions.add(resumeHandler);

		node.instructions.add(l_insns_end);
	}

	protected InsnList errorState(LabelNode label) {
		InsnList il = new InsnList();
		il.add(label);
		il.add(ASMUtils.frameSame());
		il.add(new TypeInsnNode(NEW, Type.getInternalName(IllegalStateException.class)));
		il.add(new InsnNode(DUP));
		il.add(ASMUtils.ctor(IllegalStateException.class));
		il.add(new InsnNode(ATHROW));
		return il;
	}

	protected InsnList dispatchTable() {
		InsnList il = new InsnList();
		LabelNode[] labels = resumptionPoints.toArray(new LabelNode[0]);
		il.add(new VarInsnNode(ILOAD, LV_RESUME));
		il.add(new TableSwitchInsnNode(0, resumptionPoints.size() - 1, l_error_state, labels));
		return il;
	}

	public InsnList createSnapshot() {
		InsnList il = new InsnList();

		il.add(new VarInsnNode(ALOAD, 0));  // this
		il.add(new VarInsnNode(ALOAD, 0));
		il.add(new VarInsnNode(ILOAD, LV_RESUME));
		if (parent.isVararg()) {
			il.add(new VarInsnNode(ALOAD, LV_VARARGS));
		}
		for (int i = 0; i < numOfRegisters(); i++) {
			il.add(new VarInsnNode(ALOAD, registerLocalValueIndex(i)));
		}
		il.add(parent.snapshotMethod().methodInvokeInsn());

		return il;
	}

	protected InsnList resumptionHandler(LabelNode label) {
		InsnList il = new InsnList();

		il.add(label);
		il.add(ASMUtils.frameSame1(ControlThrowable.class));

		il.add(new InsnNode(DUP));

		il.add(createSnapshot());

		// register snapshot with the control exception
		il.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(ControlThrowable.class),
				"push",
				Type.getMethodType(
						Type.VOID_TYPE,
						Type.getType(Resumable.class),
						ClassEmitter.savedStateType()).getDescriptor(),
				false));

		// rethrow
		il.add(new InsnNode(ATHROW));

		return il;
	}

	protected LabelNode _l(Object key) {
		LabelNode l = labels.get(key);

		if (l != null) {
			return l;
		}
		else {
			LabelNode nl = new LabelNode();
			labels.put(key, nl);
			return nl;
		}
	}

	public InsnList code() {
		return code;
	}

	public int newLocalVariable(int locIdx, String name, LabelNode begin, LabelNode end, Type t) {
		// FIXME: this is quite brittle!
		int idx = 3 + numOfRegisters() + (parent.isVararg() ? 1 : 0) + locIdx;
		node.localVariables.add(new LocalVariableNode(name, t.getDescriptor(), null, begin, end, idx));
		return idx;
	}

	public class ResumptionPoint {

		public final int index;

		private ResumptionPoint(int index) {
			this.index = index;
		}

		public LabelNode label() {
			return _l(this);
		}

		public InsnList save() {
			InsnList il = new InsnList();
			il.add(ASMUtils.loadInt(index));
			il.add(new VarInsnNode(ISTORE, LV_RESUME));
			return il;
		}

		public InsnList resume() {
			InsnList il = new InsnList();

			il.add(label());
			il.add(ASMUtils.frameSame());

			return il;
		}
	}

	public ResumptionPoint resumptionPoint() {
		int idx = resumptionPoints.size();
		ResumptionPoint rp = new ResumptionPoint(idx);
		resumptionPoints.add(rp.label());
		return rp;
	}

	public AbstractInsnNode loadThis() {
		return new VarInsnNode(ALOAD, 0);
	}

	public InsnList loadConstant(int idx, Class castTo) {
		return BoxedPrimitivesMethods.loadBoxedConstant(parent.context().getConst(idx), castTo);
	}

	public InsnList loadConstant(int idx) {
		return loadConstant(idx, null);
	}

	public AbstractInsnNode loadRegisterValue(int registerIndex) {
		return new VarInsnNode(ALOAD, registerLocalValueIndex(registerIndex));
	}

	public InsnList loadRegisterValue(int registerIndex, Class castTo) {
		InsnList il = new InsnList();
		il.add(loadRegisterValue(registerIndex));
		il.add(ASMUtils.checkCast(castTo));
		return il;
	}

	public InsnList loadRegister(int registerIndex, SlotState slots, Class<?> castTo) {
		Check.notNull(slots);
		Check.nonNegative(registerIndex);

		InsnList il = new InsnList();

		if (slots.isCaptured(registerIndex)) {
			il.add(loadRegisterValue(registerIndex, Upvalue.class));
			il.add(UpvalueMethods.get());
		}
		else {
			il.add(loadRegisterValue(registerIndex));
		}

		Class clazz = Object.class;

		if (castTo != null && !castTo.isAssignableFrom(clazz)) {
			il.add(ASMUtils.checkCast(castTo));
		}

		return il;
	}

	public InsnList loadRegister(int registerIndex, SlotState slots) {
		return loadRegister(registerIndex, slots, null);
	}

	public InsnList loadRegisterOrConstant(int rk, SlotState slots, Class castTo) {
		Check.notNull(slots);

		if (rk < 0) {
			// it's a constant
			return loadConstant(-rk - 1, castTo);
		}
		else {
			return loadRegister(rk, slots, castTo);
		}
	}

	public InsnList loadRegisterOrConstant(int rk, SlotState slots) {
		return loadRegisterOrConstant(rk, slots, null);
	}

	public InsnList loadNumericRegisterOrConstantValue(int rk, SlotState slots, Type requiredType) {
		InsnList il = new InsnList();

		// FIXME: this duplicates the retrieval code!
		if (rk < 0) {
			// it's a constant
			int constIndex = -rk - 1;
			Object c = parent.context().getConst(constIndex);
			if (c instanceof Number) {
				il.add(BoxedPrimitivesMethods.loadNumericValue((Number) c, requiredType));
			}
			else {
				throw new IllegalArgumentException("Constant #" + constIndex + " is not a Number: "
						+ c + " (" + (c != null ? c.getClass().getName() : "null") + ")");
			}
		}
		else {
			// it's a register
			il.add(loadRegister(rk, slots, Number.class));
			il.add(BoxedPrimitivesMethods.unbox(Number.class, requiredType));
		}

		return il;
	}

	public InsnList loadRegisterAsBoolean(int registerIndex, SlotState slots) {
		InsnList il = new InsnList();
		if (slots.typeAt(registerIndex).isSubtypeOf(LuaTypes.BOOLEAN)) {
			il.add(loadRegister(registerIndex, slots, Boolean.class));
			il.add(BoxedPrimitivesMethods.booleanValue());
		}
		else {
			il.add(loadRegister(registerIndex, slots));
			il.add(ConversionMethods.booleanValueOf());
		}
		return il;
	}

	public InsnList loadRegisters(int firstRegisterIndex, SlotState slots, int num) {
		InsnList il = new InsnList();
		for (int i = 0; i < num; i++) {
			il.add(loadRegister(firstRegisterIndex + i, slots));
		}
		return il;
	}

	public InsnList packRegistersIntoArray(int firstRegisterIndex, SlotState slots, int num) {
		InsnList il = new InsnList();

		il.add(ASMUtils.loadInt(num));
		il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
		for (int i = 0; i < num; i++) {
			il.add(new InsnNode(DUP));
			il.add(ASMUtils.loadInt(i));
			il.add(loadRegister(firstRegisterIndex + i, slots));
			il.add(new InsnNode(AASTORE));
		}

		return il;
	}

	public InsnList packRegistersUpToStackTopIntoArray(int firstRegisterIndex, SlotState slots) {
		Check.isTrue(slots.hasVarargs());

		InsnList il = new InsnList();

		int n = slots.varargPosition() - firstRegisterIndex;

		if (n == 0) {
			// just take the varargs
			il.add(loadObjectSink());
			il.add(ObjectSinkMethods.toArray());
		}
		else if (n < 0) {
			// drop n elements from the object sink
			il.add(loadObjectSink());
			il.add(new InsnNode(DUP));
			il.add(ASMUtils.loadInt(-n));
			il.add(ObjectSinkMethods.drop());
			il.add(ObjectSinkMethods.toArray());
		}
		else {
			// prepend n elements
			il.add(packRegistersIntoArray(firstRegisterIndex, slots, n));
			il.add(loadObjectSink());
			il.add(ObjectSinkMethods.toArray());
			il.add(UtilMethods.concatenateArrays());
		}

		return il;
	}

	// FIXME: come up with a better name
	public InsnList mapInvokeArgumentsToKinds(int fromIndex, SlotState slots, int desired, int actual) {
		if (desired > 0) {
			if (actual == 0) {
				// passing args through an array
				return packRegistersIntoArray(fromIndex, slots, desired - 1);
			}
			else {
				// passing args through the stack
				Check.isEq(desired, actual);
				return loadRegisters(fromIndex, slots, desired - 1);
			}
		}
		else {
			// passing args through an array, but need to extract them from the object sink
			Check.isEq(desired, actual);
			return packRegistersUpToStackTopIntoArray(fromIndex, slots);
		}
	}

	private AbstractInsnNode storeRegisterValue(int registerIndex) {
		return new VarInsnNode(ASTORE, registerLocalValueIndex(registerIndex));
	}

	public InsnList storeToRegister(int registerIndex, SlotState slots) {
		InsnList il = new InsnList();

		if (slots.isCaptured(registerIndex)) {
			il.add(loadRegisterValue(registerIndex, Upvalue.class));
			il.add(new InsnNode(SWAP));
			il.add(UpvalueMethods.set());
		}
		else {
			il.add(storeRegisterValue(registerIndex));
		}

		return il;
	}

	// TODO: name: shouldn't this be "coerce"?
	public InsnList convertRegisterToNumber(int r, SlotState st, String what) {
		InsnList il = new InsnList();

		il.add(loadRegister(r, st));
		il.add(ConversionMethods.toNumericalValue(what));
		il.add(storeToRegister(r, st));

		return il;
	}

	public InsnList convertNumericRegisterToFloat(int registerIndex, SlotState st) {
		InsnList il = new InsnList();

		il.add(loadRegister(registerIndex, st, Number.class));
		il.add(ConversionMethods.floatValueOf());
		il.add(storeToRegister(registerIndex, st));

		return il;
	}

	public AbstractInsnNode loadExecutionContext() {
		return new VarInsnNode(ALOAD, LV_CONTEXT);
	}

	private AbstractInsnNode loadState() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				Type.getInternalName(ExecutionContext.class),
				"getState",
				Type.getMethodDescriptor(
						Type.getType(LuaState.class)),
				true);
	}

	private AbstractInsnNode loadSink() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				Type.getInternalName(ExecutionContext.class),
				"getObjectSink",
				Type.getMethodDescriptor(
						Type.getType(ObjectSink.class)),
				true);
	}

	@Deprecated
	public InsnList loadLuaState() {
		InsnList il = new InsnList();
		il.add(loadExecutionContext());
		il.add(loadState());
		return il;
	}

	@Deprecated
	public InsnList loadObjectSink() {
		InsnList il = new InsnList();
		il.add(loadExecutionContext());
		il.add(loadSink());
		return il;
	}

	public AbstractInsnNode loadVarargs() {
		Check.isTrue(parent.isVararg());
		return new VarInsnNode(ALOAD, LV_VARARGS);
	}

	public InsnList loadDispatchPreamble() {
		InsnList il = new InsnList();
		il.add(loadExecutionContext());
		return il;
	}

	public InsnList retrieve_0() {
		InsnList il = new InsnList();

		il.add(loadObjectSink());
		il.add(ObjectSinkMethods.get(0));

		return il;
	}

	public InsnList retrieveAndStore(int firstIdx, SlotState slots, int num) {
		InsnList il = new InsnList();
		if (num > 0) {
			il.add(loadObjectSink());
			for (int i = 0; i < num; i++) {
				if (i + 1 < num) {
					il.add(new InsnNode(DUP));
				}
				il.add(ObjectSinkMethods.get(i));
				il.add(storeToRegister(firstIdx + i, slots));
			}
		}
		return il;
	}

	public InsnList clearRegisters(int firstIdx) {
		InsnList il = new InsnList();
		for (int r = firstIdx; r < numOfRegisters(); r++) {
			il.add(new InsnNode(ACONST_NULL));
			il.add(storeRegisterValue(r));
		}
		return il;
	}

	public InsnList getUpvalueReference(int idx) {
		InsnList il = new InsnList();
		il.add(new VarInsnNode(ALOAD, 0));
		il.add(new FieldInsnNode(
				GETFIELD,
				parent.thisClassType().getInternalName(),
				parent.getUpvalueFieldName(idx),
				Type.getDescriptor(Upvalue.class)));
		return il;
	}

	public InsnList captureRegister(int registerIndex) {
		InsnList il = new InsnList();

		il.add(loadLuaState());
		il.add(loadRegisterValue(registerIndex));
		il.add(LuaStateMethods.newUpvalue());
		il.add(storeRegisterValue(registerIndex));

		return il;
	}

	public InsnList uncaptureRegister(int registerIndex) {
		InsnList il = new InsnList();

		il.add(loadRegisterValue(registerIndex, Upvalue.class));
		il.add(UpvalueMethods.get());
		il.add(storeRegisterValue(registerIndex));

		return il;
	}

	public ClassEmitter.NestedInstanceKind nestedClosureKind(int index) {
		return parent.nestedInstanceKind(index);
	}

	protected FieldInsnNode getNestedInstanceField(int idx) {
		return parent.getNestedInstance(idx);
	}

	public InsnList cpuCheck(int cost) {
		InsnList il = new InsnList();

		ResumptionPoint rp = resumptionPoint();
		il.add(rp.save());
		il.add(loadLuaState());
		il.add(ASMUtils.loadInt(cost));
		il.add(LuaStateMethods.checkCpu());
		il.add(rp.resume());

		return il;
	}

}
