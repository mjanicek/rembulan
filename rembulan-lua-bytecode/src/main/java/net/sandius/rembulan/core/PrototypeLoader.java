package net.sandius.rembulan.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class PrototypeLoader implements PrototypeVisitable {

	protected final BinaryChunkInputStream is;

	public PrototypeLoader(BinaryChunkInputStream stream) {
		this.is = Objects.requireNonNull(stream);
	}

	public static PrototypeLoader fromInputStream(InputStream stream) throws IOException {
		return new PrototypeLoader(BinaryChunkInputStream.fromInputStream(stream));
	}

	@Override
	public void accept(PrototypeVisitor pv) {
		try {
			acceptHeader(pv);
			acceptCode(pv);
			acceptConstants(pv);
			acceptUpvalues(pv);
			acceptNestedPrototypes(pv);
			acceptDebugInfo(pv);

			if (pv != null) pv.visitEnd();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void acceptHeader(PrototypeVisitor pv) throws IOException {
		acceptSource(pv);
		acceptSize(pv);
	}

	protected void acceptSource(PrototypeVisitor pv) throws IOException {
		String source = is.readString();
		int firstLineDefined = is.readInt();
		int lastLineDefined = is.readInt();

		if (pv != null) pv.visitSource(source, firstLineDefined, lastLineDefined);
	}

	protected void acceptSize(PrototypeVisitor pv) throws IOException {
		int numOfParameters = is.readUnsignedByte();
		boolean isVararg = is.readBoolean();
		int maxStackSize = is.readUnsignedByte();

		if (pv != null) pv.visitSize(numOfParameters, isVararg, maxStackSize);
	}

	protected void acceptCode(PrototypeVisitor pv) throws IOException {
		int[] insns = is.readIntArray();
		if (pv != null) {
			for (int insn : insns) {
				int opcode = OpCode.opCode(insn);

				int a = OpCode.arg_A(insn);
				int b = OpCode.arg_B(insn);
				int c = OpCode.arg_C(insn);
				int bx = OpCode.arg_Bx(insn);
				int sbx = OpCode.arg_sBx(insn);
				int ax = OpCode.arg_Ax(insn);

				switch (OpCode.getOpMode(opcode)) {
					case OpCode.iABC:  pv.visitABCInstruction(opcode, a, b, c); break;
					case OpCode.iABx:  pv.visitABxInstruction(opcode, a, bx); break;
					case OpCode.iAsBx: pv.visitAsBxInstruction(opcode, a, sbx); break;
					case OpCode.iAx:   pv.visitAxInstruction(opcode, ax); break;
					default: throw new IllegalArgumentException("Illegal instruction: " + Integer.toHexString(insn));
				}
			}
		}
	}

	protected void acceptConstants(PrototypeVisitor pv) throws IOException {
		int n = is.readInt();
		for (int i = 0; i < n; i++) {
			int tag = is.readUnsignedByte();
			switch (tag) {
				case BinaryChunkConstants.CONST_NIL:
					if (pv != null) pv.visitNilConst(); break;
				case BinaryChunkConstants.CONST_BOOLEAN: {
					boolean value = is.readBoolean();
					if (pv != null) pv.visitBooleanConst(value);
					break;
				}

				case BinaryChunkConstants.CONST_INTEGER: {
					long value = is.readInteger();
					if (pv != null) pv.visitIntegerConst(value);
					break;
				}
				case BinaryChunkConstants.CONST_FLOAT: {
					double value = is.readFloat();
					if (pv != null) pv.visitFloatConst(value);
					break;
				}

				case BinaryChunkConstants.CONST_SHORT_STRING: {
					String value = is.readShortString();
					if (pv != null) pv.visitStringConst(value);
					break;
				}
				case BinaryChunkConstants.CONST_LONG_STRING: {
					String value = is.readLongString();
					if (pv != null) pv.visitStringConst(value);
					break;
				}

				default: throw new IllegalStateException("Illegal constant type: " + tag);
			}
		}
	}

	protected void acceptUpvalues(PrototypeVisitor pv) throws IOException {
		int n = is.readInt();
		for (int i = 0; i < n; i++) {
			boolean inStack = is.readBoolean();
			int idx = is.readUnsignedByte();
			if (pv != null) pv.visitUpvalue(inStack, idx);
		}
	}

	protected void acceptNestedPrototypes(PrototypeVisitor pv) throws IOException {
		int n = is.readInt();
		for (int i = 0; i < n; i++) {
			PrototypeVisitor npv = pv != null ? pv.visitNestedPrototype() : null;
			accept(npv);
		}
	}

	protected void acceptDebugInfo(PrototypeVisitor pv) throws IOException {
		acceptLineInfo(pv);
		acceptLocalVariables(pv);
		acceptUpvalueNames(pv);
	}

	protected void acceptLineInfo(PrototypeVisitor pv) throws IOException {
		int[] lineInfo = is.readIntArray();

		if (pv != null) {
			for (int line : lineInfo) {
				pv.visitLine(line);
			}
		}
	}

	protected void acceptLocalVariables(PrototypeVisitor pv) throws IOException {
		int n = is.readInt();
		for (int i = 0; i < n; i++) {
			String name = is.readString();
			int start = is.readInt();
			int end = is.readInt();
			if (pv != null) pv.visitLocalVariable(name, start, end);
		}
	}

	protected void acceptUpvalueNames(PrototypeVisitor pv) throws IOException {
		int n = is.readInt();
		for (int i = 0; i < n; i++) {
			String uvn = is.readString();
			if (pv != null) pv.visitUpvalueName(uvn);
		}
	}

}
