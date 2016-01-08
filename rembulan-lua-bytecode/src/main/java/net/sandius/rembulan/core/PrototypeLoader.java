/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/

package net.sandius.rembulan.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class PrototypeLoader {

	// type constants
	public static final int LUA_TINT            = (-2);
	public static final int LUA_TNONE			= (-1);
	public static final int LUA_TNIL			= 0;
	public static final int LUA_TBOOLEAN		= 1;
	public static final int LUA_TLIGHTUSERDATA	= 2;
	public static final int LUA_TNUMBER			= 3;
	public static final int LUA_TSTRING			= 4;
	public static final int LUA_TTABLE			= 5;
	public static final int LUA_TFUNCTION		= 6;
	public static final int LUA_TUSERDATA		= 7;
	public static final int LUA_TTHREAD			= 8;
	public static final int LUA_TVALUE          = 9;

	public static final int LUA_TSHRSTR = LUA_TSTRING | (0 << 4);  // short strings
	public static final int LUA_TLNGSTR = LUA_TSTRING | (1 << 4);  // long strings

	public static final int LUA_TNUMFLT = LUA_TNUMBER | (0 << 4);  // float numbers
	public static final int LUA_TNUMINT = LUA_TNUMBER | (1 << 4);  // integer numbers

	/** input stream from which we are loading */
	protected final LuaChunkInputStream is;

	public PrototypeLoader(LuaChunkInputStream stream) {
		this.is = Objects.requireNonNull(stream);
	}

	public static PrototypeLoader fromInputStream(InputStream stream) throws IOException {
		return new PrototypeLoader(LuaChunkInputStream.fromInputStream(stream));
	}

	public void accept(PrototypeVisitor pv) throws IOException {
		acceptHeader(pv);
		acceptCode(pv);
		acceptConstants(pv);
		acceptUpvalues(pv);
		acceptNestedPrototypes(pv);
		acceptDebugInfo(pv);

		if (pv != null) pv.visitEnd();
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
				case LUA_TNIL:
					if (pv != null) pv.visitNilConst(); break;
				case LUA_TBOOLEAN: {
					boolean value = is.readBoolean();
					if (pv != null) pv.visitBooleanConst(value);
					break;
				}

				case LUA_TNUMINT: {
					long value = is.readInteger();
					if (pv != null) pv.visitIntegerConst(value);
					break;
				}
				case LUA_TNUMFLT: {
					double value = is.readFloat();
					if (pv != null) pv.visitFloatConst(value);
					break;
				}

				case LUA_TSHRSTR: {
					String value = is.readShortString();
					if (pv != null) pv.visitStringConst(value);
					break;
				}
				case LUA_TLNGSTR: {
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

	@Deprecated
	public static Prototype undump(InputStream stream) throws IOException {
		PrototypeLoader loader = fromInputStream(stream);
		PrototypeBuilderVisitor visitor = new PrototypeBuilderVisitor();
		loader.accept(visitor);
		return visitor.get();
	}

}
