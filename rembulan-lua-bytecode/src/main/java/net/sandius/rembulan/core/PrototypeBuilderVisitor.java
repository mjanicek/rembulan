package net.sandius.rembulan.core;

import net.sandius.rembulan.util.IntBuffer;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class PrototypeBuilderVisitor extends PrototypeVisitor {

	private int numParams;
	private boolean vararg;
	private int maxStackSize;
	private String source;
	private int firstLineDefined;
	private int lastLineDefined;

	private IntBuffer code;
	private ArrayList<Object> consts;
	private ArrayList<Upvalue.Desc> upvalueDesc;

	private ArrayList<PrototypeBuilderVisitor> nested;

	private IntBuffer lines;
	private ArrayList<String> upvalueNames;
	private ArrayList<LocalVariable> locals;

	private Prototype result;

	@Override
	public void visit(int numParams, boolean vararg, int maxStackSize, String source, int firstLineDefined, int lastLineDefined) {
		this.numParams = numParams;
		this.vararg = vararg;
		this.maxStackSize = maxStackSize;
		this.source = source;
		this.firstLineDefined = firstLineDefined;
		this.lastLineDefined = lastLineDefined;

		this.code = new IntBuffer();
		this.consts = new ArrayList<>();
		this.nested = new ArrayList<>();
		this.upvalueDesc = new ArrayList<>();

		this.lines = new IntBuffer();
		this.upvalueNames = new ArrayList<>();
		this.locals = new ArrayList<>();

		result = null;
	}

	@Override
	public void visitEnd() {
		Constants constants = new ArrayBackedConstants(ReadOnlyArray.fromCollection(Object.class, consts));

		ArrayList<Prototype> nestedPrototypes = new ArrayList<>();
		for (PrototypeBuilderVisitor visitor : nested) {
			nestedPrototypes.add(Objects.requireNonNull(visitor.get()));
		}

		ArrayList<Upvalue.Desc> upvals = new ArrayList<>();
		Iterator<String> names = upvalueNames.iterator();
		for (Upvalue.Desc uvd : upvalueDesc) {
			String name = names.hasNext() ? names.next() : null;
			upvals.add(new Upvalue.Desc(name, uvd.inStack, uvd.index));
		}

		result = new Prototype(
				constants,
				code.toVector(),
				ReadOnlyArray.fromCollection(Prototype.class, nestedPrototypes),
				lines.toVector(),
				ReadOnlyArray.fromCollection(LocalVariable.class, locals),
				ReadOnlyArray.fromCollection(Upvalue.Desc.class, upvals),
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
		Objects.requireNonNull(value);
		consts.add(value);
	}

	@Override
	public void visitUpvalue(boolean inStack, int index) {
		upvalueDesc.add(new Upvalue.Desc(null, inStack, index));
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
		Objects.requireNonNull(name);
		upvalueNames.add(name);
	}

	@Override
	public void visitLocalVariable(String name, int beginPC, int endPC) {
		locals.add(new LocalVariable(name, beginPC, endPC));
	}

}
