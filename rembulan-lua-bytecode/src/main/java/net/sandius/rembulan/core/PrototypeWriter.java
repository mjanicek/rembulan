package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class PrototypeWriter extends PrototypeVisitor {

	public final BinaryChunkOutputStream out;

	private final Buffer sourceHeader;
	private final Buffer sigHeader;

	private final SequenceBuffer code;
	private final SequenceBuffer constants;
	private final SequenceBuffer upvalues;

	private final ArrayList<ByteArrayOutputStream> nested;

	private final SequenceBuffer lines;
	private final SequenceBuffer locals;
	private final SequenceBuffer upvalueNames;

	class Buffer {
		private final ByteArrayOutputStream data = new ByteArrayOutputStream();
		public final BinaryChunkOutputStream stream = new BinaryChunkOutputStream(this.data, out.getFormat());

		public void reset() {
			data.reset();
		}

		public void write() throws IOException {
			data.writeTo(out);
		}
	}

	class SequenceBuffer extends Buffer {
		private int count = 0;

		public void inc() {
			count += 1;
		}

		public void reset() {
			super.reset();
			count = 0;
		}

		@Override
		public void write() throws IOException {
			out.writeInt(count);
			super.write();
		}
	}

	public PrototypeWriter(BinaryChunkOutputStream out) {
		this.out = Objects.requireNonNull(out);

		this.sourceHeader = new Buffer();
		this.sigHeader = new Buffer();

		this.code = new SequenceBuffer();
		this.constants = new SequenceBuffer();
		this.upvalues = new SequenceBuffer();

		this.nested = new ArrayList<>();

		this.lines = new SequenceBuffer();
		this.locals = new SequenceBuffer();
		this.upvalueNames = new SequenceBuffer();
	}

	@Override
	public void visitSize(int numParams, boolean vararg, int maxStackSize) {
		try {
			sigHeader.reset();
			sigHeader.stream.writeByte(numParams);
			sigHeader.stream.writeBoolean(vararg);
			sigHeader.stream.writeByte(maxStackSize);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitSource(String source, int firstLineDefined, int lastLineDefined) {
		try {
			sourceHeader.reset();
			sourceHeader.stream.writeString(source);
			sourceHeader.stream.writeInt(firstLineDefined);
			sourceHeader.stream.writeInt(lastLineDefined);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		try {
			code.stream.writeInstruction(insn);
			code.inc();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitNilConst() {
		try {
			constants.stream.writeByte(BinaryChunkConstants.CONST_NIL);
			constants.inc();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitBooleanConst(boolean value) {
		try {
			constants.stream.writeByte(BinaryChunkConstants.CONST_BOOLEAN);
			constants.stream.writeBoolean(value);
			constants.inc();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitIntegerConst(long value) {
		try {
			constants.stream.writeByte(BinaryChunkConstants.CONST_INTEGER);
			constants.stream.writeInteger(value);
			constants.inc();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitFloatConst(double value) {
		try {
			constants.stream.writeByte(BinaryChunkConstants.CONST_FLOAT);
			constants.stream.writeFloat(value);
			constants.inc();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitStringConst(String value) {
		Check.notNull(value);
		try {
			byte[] bytes = value.getBytes();
			if (bytes.length < 0xff) {
				constants.stream.writeByte(BinaryChunkConstants.CONST_SHORT_STRING);
				constants.stream.writeShortString(bytes);
			}
			else {
				constants.stream.writeByte(BinaryChunkConstants.CONST_LONG_STRING);
				constants.stream.writeLongString(bytes);
			}
			constants.inc();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitUpvalue(boolean inStack, int index) {
		try {
			upvalues.stream.writeBoolean(inStack);
			upvalues.stream.writeByte(index);
			upvalues.inc();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PrototypeVisitor visitNestedPrototype() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrototypeWriter pw = new PrototypeWriter(new BinaryChunkOutputStream(baos, out.getFormat()));
		nested.add(baos);
		return pw;
	}

	@Override
	public void visitLine(int line) {
		try {
			lines.stream.writeInt(line);
			lines.inc();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitUpvalueName(String name) {
		try {
			upvalueNames.stream.writeString(name);
			upvalueNames.inc();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitLocalVariable(String name, int beginPC, int endPC) {
		try {
			locals.stream.writeString(name);
			locals.stream.writeInt(beginPC);
			locals.stream.writeInt(endPC);
			locals.inc();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitEnd() {
		try {
			sourceHeader.write();
			sigHeader.write();

			code.write();
			constants.write();
			upvalues.write();

			out.writeInt(nested.size());
			for (ByteArrayOutputStream nestedBaos : nested) {
				nestedBaos.writeTo(out);
			}

			lines.write();
			locals.write();
			upvalueNames.write();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
