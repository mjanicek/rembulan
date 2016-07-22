package net.sandius.rembulan.lbc;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntBuffer;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.ArrayList;
import java.util.Iterator;

public class PrototypeBuilderVisitor extends PrototypeVisitor {

	private int numParams;
	private boolean vararg;
	private int maxStackSize;
	private String source;
	private int firstLineDefined;
	private int lastLineDefined;

	private IntBuffer code;
	private ArrayList<Object> consts;
	private ArrayList<Prototype.UpvalueDesc> upvalueDesc;

	private ArrayList<PrototypeBuilderVisitor> nested;

	private IntBuffer lines;
	private ArrayList<String> upvalueNames;
	private ArrayList<Prototype.LocalVariable> locals;

	private Prototype result;

	@Override
	public void visitSize(int numParams, boolean vararg, int maxStackSize) {
		this.numParams = numParams;
		this.vararg = vararg;
		this.maxStackSize = maxStackSize;

		this.code = IntBuffer.empty();
		this.consts = new ArrayList<>();
		this.nested = new ArrayList<>();
		this.upvalueDesc = new ArrayList<>();

		this.lines = IntBuffer.empty();
		this.upvalueNames = new ArrayList<>();
		this.locals = new ArrayList<>();

		result = null;
	}

	@Override
	public void visitSource(String source, int firstLineDefined, int lastLineDefined) {
		this.source = source;
		this.firstLineDefined = firstLineDefined;
		this.lastLineDefined = lastLineDefined;
	}

	@Override
	public void visitEnd() {
		ReadOnlyArray<Object> constants = ReadOnlyArray.fromCollection(Object.class, consts);

		ArrayList<Prototype> nestedPrototypes = new ArrayList<>();
		for (PrototypeBuilderVisitor visitor : nested) {
			nestedPrototypes.add(Check.notNull(visitor.get()));
		}

		ArrayList<Prototype.UpvalueDesc> upvals = new ArrayList<>();
		Iterator<String> names = upvalueNames.iterator();
		for (Prototype.UpvalueDesc uvd : upvalueDesc) {
			String name = names.hasNext() ? names.next() : null;
			upvals.add(new Prototype.UpvalueDesc(name, uvd.inStack, uvd.index));
		}

		result = new Prototype(
				constants,
				code.toVector(),
				ReadOnlyArray.fromCollection(Prototype.class, nestedPrototypes),
				lines.toVector(),
				ReadOnlyArray.fromCollection(Prototype.LocalVariable.class, locals),
				ReadOnlyArray.fromCollection(Prototype.UpvalueDesc.class, upvals),
				source,
				firstLineDefined,
				lastLineDefined,
				numParams,
				vararg,
				maxStackSize);
	}

	// may return null
	public Prototype get() {
		return result;
	}

	@Override
	public void visitABCInstruction(int opcode, int a, int b, int c) {
		visitInstruction(OpCode.fromABC(opcode, a, b, c));
	}

	@Override
	public void visitABxInstruction(int opcode, int a, int bx) {
		visitInstruction(OpCode.fromABx(opcode, a, bx));
	}

	@Override
	public void visitAsBxInstruction(int opcode, int a, int sbx) {
		visitInstruction(OpCode.fromAsBx(opcode, a, sbx));
	}

	@Override
	public void visitAxInstruction(int opcode, int ax) {
		visitInstruction(OpCode.fromAx(opcode, ax));
	}

	public void visitInstruction(int insn) {
		code.append(insn);
	}

	@Override
	public void visitNilConst() {
		consts.add(null);
	}

	@Override
	public void visitBooleanConst(boolean value) {
		consts.add(value);
	}

	@Override
	public void visitIntegerConst(long value) {
		consts.add(value);
	}

	@Override
	public void visitFloatConst(double value) {
		consts.add(value);
	}

	@Override
	public void visitStringConst(String value) {
		Check.notNull(value);
		consts.add(value);
	}

	@Override
	public void visitUpvalue(boolean inStack, int index) {
		upvalueDesc.add(new Prototype.UpvalueDesc(null, inStack, index));
	}

	@Override
	public PrototypeVisitor visitNestedPrototype() {
		PrototypeBuilderVisitor visitor = new PrototypeBuilderVisitor();
		nested.add(visitor);
		return visitor;
	}

	@Override
	public void visitLine(int line) {
		lines.append(line);
	}

	@Override
	public void visitUpvalueName(String name) {
		Check.notNull(name);
		upvalueNames.add(name);
	}

	@Override
	public void visitLocalVariable(String name, int beginPC, int endPC) {
		locals.add(new Prototype.LocalVariable(name, beginPC, endPC));
	}

}
