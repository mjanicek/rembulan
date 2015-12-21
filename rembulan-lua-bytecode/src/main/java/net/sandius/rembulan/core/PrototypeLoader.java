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

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

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

	/** Signature byte indicating the file is a compiled binary chunk */
	public static final byte[] LUA_SIGNATURE	= { '\033', 'L', 'u', 'a' };

	/** Data to catch conversion errors */
	public static final byte[] LUAC_TAIL = { (byte) 0x19, (byte) 0x93, '\r', '\n', (byte) 0x1a, '\n', };

	public static final long TestIntLE = 0x7856000000000000L;
	public static final long TestIntBE = 0x0000000000005678L;

	public static final double TestNum = 370.5;
	public static final long TestNumBitsLE = 0x0000000000287740L;
	public static final long TestNumBitsBE = 0x4077280000000000L;


	/** Name for compiled chunks */
	public static final String SOURCE_BINARY_STRING = "binary string";


	/** for header of binary files -- this is Lua 5.3 */
	public static final int LUAC_VERSION		= 0x53;

	/** for header of binary files -- this is the official format */
	public static final int LUAC_FORMAT		= 0;

	/** size of header of binary files */
	public static final int LUAC_HEADERSIZE		= 12;

	// values read from the header
	private int     luacVersion;
	private int     luacFormat;
	private boolean littleEndian;
	private int     luacSizeofInt;
	private int     luacSizeofSizeT;
	private int     luacSizeofInstruction;
	private int     luacSizeofLuaInteger;
	private int     luacSizeofLuaNumber;

	/** input stream from which we are loading */
	public final DataInputStream is;

	private final ConstantsBuilder.Factory constantsBuilderFactory;

	public PrototypeLoader(DataInputStream stream, ConstantsBuilder.Factory constantsBuilderFactory) {
		Check.notNull(stream);
		Check.notNull(constantsBuilderFactory);

		this.is = stream;
		this.constantsBuilderFactory = constantsBuilderFactory;
	}

	public PrototypeLoader(InputStream stream) {
		this(new DataInputStream(stream), ArrayBackedConstants.Builder.FACTORY);
	}

	/**
	 * Load a signed 32-bit integer from the input stream.
	 *
	 * @return the int value loaded.
	 */
	int loadInt32() throws IOException {
		int i = is.readInt();
		return littleEndian ? Integer.reverseBytes(i) : i;
	}

	/**
	 * Load a signed 64-bit integer from the input stream.
	 *
	 * @return the long value loaded.
	 */
	long loadInt64() throws IOException {
		long l = is.readLong();
		return littleEndian ? Long.reverseBytes(l) : l;
	}

	/**
	 * Load an array of signed 32-bit integers from the input stream.
	 *
	 * @return the array of int values loaded.
	 */
	int[] loadIntVector() throws IOException {
		int n = loadInt32();
		int[] array = new int[n];
		for (int i = 0; i < n; i++) {
			array[i] = loadInt32();
		}
		return array;
	}

	boolean loadBoolean() throws IOException {
		return is.readUnsignedByte() != 0;
	}

	int loadSizeT() throws IOException {
		return this.luacSizeofSizeT == 8 ? (int) loadInt64() : loadInt32();
	}

	/** Load a string from the input stream.
	 *
	 * @return the string value loaded.
	 */
	String loadString() throws IOException {
		int hx = is.readUnsignedByte();
		int size = hx == 0xff ? loadSizeT() : hx;

		if (size == 0) {
			return null;
		}

		assert (size > 0);

		size -= 1;  // trailing '\0' is not stored

		byte[] bytes = new byte[size];
		is.readFully(bytes, 0, size);

		char[] chars = new char[size];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) (bytes[i] & 0xff);
		}

		return String.valueOf(chars);
	}

	public long loadInteger() throws IOException {
		return loadInt64();
	}

	public double loadFloat() throws IOException {
		return Double.longBitsToDouble(loadInt64());
	}

	public void loadConstant(ConstantsBuilder bld) throws IOException {
		byte tag = is.readByte();
		switch (tag) {
			case LUA_TNIL:     bld.addNil(); break;
			case LUA_TBOOLEAN: bld.addBoolean(loadBoolean()); break;

			case LUA_TNUMINT:  bld.addInteger(loadInteger()); break;
			case LUA_TNUMFLT:  bld.addFloat(loadFloat()); break;

			case LUA_TSHRSTR:
			case LUA_TLNGSTR:
				// TODO: is this correct for long strings?
				bld.addString(loadString());
				break;

			default: throw new IllegalStateException("Illegal constant type: " + tag);
		}
	}

	Constants loadConstants() throws IOException {
		int n = loadInt32();
		ConstantsBuilder bld = constantsBuilderFactory.newBuilder();
		for (int i = 0; i < n; i++) {
			loadConstant(bld);
		}
		return bld.build();
	}

	Prototype[] loadNestedPrototypes(String source) throws IOException {
		int n = loadInt32();
		Prototype[] array = new Prototype[n];
		for (int i = 0; i < n; i++) {
			array[i] = loadFunction(source);
		}
		return array;
	}

	Upvalue.Desc[] loadUpvalues() throws IOException {
		int n = loadInt32();
		Upvalue.Desc[] array = new Upvalue.Desc[n];
		for (int i = 0; i < n; i++) {
			boolean inStack = loadBoolean();
			int idx = ((int) is.readByte()) & 0xff;
			array[i] = new Upvalue.Desc(null, inStack, idx);
		}
		return array;
	}

	LocalVariable[] loadLocals() throws IOException {
		int n = loadInt32();
		LocalVariable[] array = new LocalVariable[n];
		for (int i = 0; i < n; i++) {
			String name = loadString();
			int start = loadInt32();
			int end = loadInt32();
			array[i] = new LocalVariable(name, start, end);
		}
		return array;
	}

	public Prototype loadFunction(String src) throws IOException {
		String source = loadString();
		if (source == null) source = src;

		int firstLineDefined = loadInt32();
		int lastLineDefined = loadInt32();
		int numOfParameters = is.readUnsignedByte();
		boolean isVararg = loadBoolean();
		int maxStackSize = is.readUnsignedByte();

		int[] code = loadIntVector();
		Constants constants = loadConstants();
		Upvalue.Desc[] upvalues = loadUpvalues();
		Prototype[] nestedPrototypes = loadNestedPrototypes(source);

		// debug information

		int[] lineInfo = loadIntVector();
		LocalVariable[] locals = loadLocals();

		// fill in upvalue names
		int n = loadInt32();
		for (int i = 0; i < n; i++) {
			upvalues[i] = new Upvalue.Desc(loadString(), upvalues[i].inStack, upvalues[i].index);
		}

		// TODO: add support for verifiers

		return new Prototype(
				constants,
				IntVector.wrap(code),
				ReadOnlyArray.wrap(nestedPrototypes),
				IntVector.wrap(lineInfo),
				ReadOnlyArray.wrap(locals),
				ReadOnlyArray.wrap(upvalues),
				source,
				firstLineDefined,
				lastLineDefined,
				numOfParameters,
				isVararg,
				maxStackSize
		);
	}

	/**
	 * Load the lua chunk header values.
	 * @throws IOException if an i/o exception occurs.
	 */
	public void loadHeader() throws IOException {
		luacVersion = is.readByte();
		luacFormat = is.readByte();

		for (int i = 0; i < LUAC_TAIL.length; ++i) {
			if (is.readByte() != LUAC_TAIL[i]) throw new RuntimeException("Unexpected byte in LuaC tail of header, index=" + i);
		}

		luacSizeofInt = is.readByte();
		luacSizeofSizeT = is.readByte();
		luacSizeofInstruction = is.readByte();
		luacSizeofLuaInteger = is.readByte();
		luacSizeofLuaNumber = is.readByte();

		// check endianness
        long ti = is.readLong();

		if ((ti == TestIntLE) == (ti == TestIntBE)) {
			throw new IllegalArgumentException("Endianness mismatch: 0x" + Long.toHexString(ti));
        }
        else {
			littleEndian = (ti == TestIntLE);
        }

        // TODO: use loadNumber here!
        long tn = is.readLong();

		if (littleEndian) {
			// TODO: this!
        }
        else {
			// no conversion necessary
			// TODO
//			Double.longBitsToDouble(tn);
        }

		boolean isClosure = loadBoolean();
		// TODO: require true
	}

	/**
	 * Load input stream as a lua binary chunk if the first 4 bytes are the lua binary signature.
	 * @param stream InputStream to read, after having read the first byte already
	 * @return {@link Prototype} that was loaded, or null if the first 4 bytes were not the lua signature.
	 * @throws IOException if an IOException occurs
	 */
	public static Prototype undump(InputStream stream) throws IOException {

		// check rest of signature
		if (stream.read() != LUA_SIGNATURE[0]
				|| stream.read() != LUA_SIGNATURE[1]
				|| stream.read() != LUA_SIGNATURE[2]
				|| stream.read() != LUA_SIGNATURE[3]) {
			return null;
		}

		// load file as a compiled chunk
		PrototypeLoader s = new PrototypeLoader(stream);
		s.loadHeader();

		return s.loadFunction(null);
	}

	/**
	 * Construct a source name from a supplied chunk name
	 * @param name String name that appears in the chunk
	 * @return source file name
	 */
	public static String getSourceName(String name) {
		if (name.startsWith("@") || name.startsWith("=")) {
			name = name.substring(1);
		}
		else if (name.startsWith("\033")) {
			name = SOURCE_BINARY_STRING;
		}
		return name;
	}

}
