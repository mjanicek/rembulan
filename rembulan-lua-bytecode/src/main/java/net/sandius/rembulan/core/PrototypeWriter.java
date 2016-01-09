package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class PrototypeWriter extends PrototypeVisitor {

	public final ByteOrder byteOrder;
	public final BinaryChunkOutputStream out;

	private final int sizeOfInt;
	private final int sizeOfSizeT;
	private final int sizeOfInstruction;
	private final int sizeOfLuaInteger;
	private final int sizeOfLuaFloat;

	private boolean signatureWritten;

	private final BinaryChunkOutputBuffer sourceHeader;
	private final BinaryChunkOutputBuffer sigHeader;

	private final BinaryChunkOutputBuffer code;
	private final BinaryChunkOutputBuffer constants;
	private final BinaryChunkOutputBuffer upvalues;

	private final ArrayList<ByteArrayOutputStream> nested;

	private final BinaryChunkOutputBuffer lines;
	private final BinaryChunkOutputBuffer locals;
	private final BinaryChunkOutputBuffer upvalueNames;

	class BinaryChunkOutputBuffer {
		private int count;
		private final ByteArrayOutputStream data;
		public final BinaryChunkOutputStream stream;

		public BinaryChunkOutputBuffer() {
			this.count = 0;
			this.data = new ByteArrayOutputStream();
			this.stream = new BinaryChunkOutputStream(this.data, byteOrder, sizeOfInt, sizeOfSizeT, sizeOfInstruction, sizeOfLuaInteger, sizeOfLuaFloat);
		}

		public void reset() {
			data.reset();
		}

		public void inc() {
			count += 1;
		}

		public void writeToAsData(OutputStream out) throws IOException {
			data.writeTo(out);
		}

		public void writeToAsSequence(BinaryChunkOutputStream out) throws IOException {
			out.writeInt(count);
			data.writeTo(out);
		}
	}

	public PrototypeWriter(OutputStream out, ByteOrder byteOrder, int sizeOfInt, int sizeOfSizeT, int sizeOfInstruction, int sizeOfLuaInteger, int sizeOfLuaFloat) {
		Check.notNull(out);

		this.out = new BinaryChunkOutputStream(out, byteOrder, sizeOfInt, sizeOfSizeT, sizeOfInstruction, sizeOfLuaInteger, sizeOfLuaFloat);

		this.signatureWritten = false;

		this.byteOrder = byteOrder;
		this.sizeOfInt = sizeOfInt;
		this.sizeOfSizeT = sizeOfSizeT;
		this.sizeOfInstruction = sizeOfInstruction;
		this.sizeOfLuaInteger = sizeOfLuaInteger;
		this.sizeOfLuaFloat = sizeOfLuaFloat;

		this.sourceHeader = new BinaryChunkOutputBuffer();
		this.sigHeader = new BinaryChunkOutputBuffer();

		this.code = new BinaryChunkOutputBuffer();
		this.constants = new BinaryChunkOutputBuffer();
		this.upvalues = new BinaryChunkOutputBuffer();

		this.nested = new ArrayList<>();

		this.lines = new BinaryChunkOutputBuffer();
		this.locals = new BinaryChunkOutputBuffer();
		this.upvalueNames = new BinaryChunkOutputBuffer();
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
			sigHeader.reset();
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
		PrototypeWriter pw = new PrototypeWriter(baos, byteOrder, sizeOfInt, sizeOfSizeT, sizeOfInstruction, sizeOfLuaInteger, sizeOfLuaFloat);
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
			if (!signatureWritten) {
				out.writeHeader();
				signatureWritten = true;
			}

			sourceHeader.writeToAsData(out);
			sigHeader.writeToAsData(out);

			code.writeToAsSequence(out);
			constants.writeToAsSequence(out);
			upvalues.writeToAsSequence(out);

			out.writeInt(nested.size());
			for (ByteArrayOutputStream nestedBaos : nested) {
				nestedBaos.writeTo(out);
			}

			lines.writeToAsSequence(out);
			locals.writeToAsSequence(out);
			upvalueNames.writeToAsSequence(out);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
