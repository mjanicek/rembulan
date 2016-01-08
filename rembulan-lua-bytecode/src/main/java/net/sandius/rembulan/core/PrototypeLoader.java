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

	/**
	 * Load an array of signed 32-bit integers from the input stream.
	 *
	 * @return the array of int values loaded.
	 */
	private int[] loadIntVector() throws IOException {
		int n = is.readInt();
		int[] array = new int[n];
		for (int i = 0; i < n; i++) {
			array[i] = is.readInt();
		}
		return array;
	}

	public void accept(PrototypeVisitor visitor) throws IOException {
		String source = is.readString();
//		if (source == null) source = src;  // TODO

		int firstLineDefined = is.readInt();
		int lastLineDefined = is.readInt();
		int numOfParameters = is.readUnsignedByte();
		boolean isVararg = is.readBoolean();
		int maxStackSize = is.readUnsignedByte();

		visitor.visit(numOfParameters, isVararg, maxStackSize, source, firstLineDefined, lastLineDefined);

		// code
		for (int insn : loadIntVector()) {
			visitor.visitInstruction(insn);
		}

		// constants
		{
			int n = is.readInt();
			for (int i = 0; i < n; i++) {
				int tag = is.readUnsignedByte();
				switch (tag) {
					case LUA_TNIL:     visitor.visitNilConst(); break;
					case LUA_TBOOLEAN: visitor.visitBooleanConst(is.readBoolean()); break;

					case LUA_TNUMINT:  visitor.visitIntegerConst(is.readInteger()); break;
					case LUA_TNUMFLT:  visitor.visitFloatConst(is.readFloat()); break;

					case LUA_TSHRSTR:
					case LUA_TLNGSTR:
						// TODO: is this correct for long strings?
						visitor.visitStringConst(is.readString());
						break;

					default: throw new IllegalStateException("Illegal constant type: " + tag);
				}
			}
		}

		// upvalues
		{
			int n = is.readInt();
			for (int i = 0; i < n; i++) {
				boolean inStack = is.readBoolean();
				int idx = is.readUnsignedByte();
				visitor.visitUpvalue(inStack, idx);
			}
		}

		// nested prototypes
		{
			int n = is.readInt();
			for (int i = 0; i < n; i++) {
				PrototypeVisitor pv = visitor.visitNestedPrototype();
				accept(pv);
			}
		}

		// debug information
		{
			int[] lineInfo = loadIntVector();

			for (int line : lineInfo) {
				visitor.visitLine(line);
			}

			int n = is.readInt();
			for (int i = 0; i < n; i++) {
				String name = is.readString();
				int start = is.readInt();
				int end = is.readInt();
				visitor.visitLocalVariable(name, start, end);
			}

			n = is.readInt();
			for (int i = 0; i < n; i++) {
				String uvn = is.readString();
				visitor.visitUpvalueName(uvn);
			}

		}

		visitor.visitEnd();
	}

	public static PrototypeLoader fromInputStream(InputStream stream) throws IOException {
		return new PrototypeLoader(LuaChunkInputStream.fromInputStream(stream));
	}

	@Deprecated
	public static Prototype undump(InputStream stream) throws IOException {
		PrototypeLoader loader = fromInputStream(stream);
		PrototypeBuilderVisitor visitor = new PrototypeBuilderVisitor();
		loader.accept(visitor);
		return visitor.get();
	}

}
