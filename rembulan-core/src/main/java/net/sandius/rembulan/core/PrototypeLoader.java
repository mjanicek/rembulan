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

import net.sandius.rembulan.util.IntVector;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PrototypeLoader {

	/** format corresponding to non-number-patched lua, all numbers are floats or doubles */
	public static final int NUMBER_FORMAT_FLOATS_OR_DOUBLES    = 0;

	/** format corresponding to non-number-patched lua, all numbers are ints */
	public static final int NUMBER_FORMAT_INTS_ONLY            = 1;

	/** format corresponding to number-patched lua, all numbers are 32-bit (4 byte) ints */
	public static final int NUMBER_FORMAT_NUM_PATCH_INT32      = 4;

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

	/** The character encoding to use for file encoding.  Null means the default encoding */
	public static String encoding = null;

	/** Signature byte indicating the file is a compiled binary chunk */
	public static final byte[] LUA_SIGNATURE	= { '\033', 'L', 'u', 'a' };

	/** Data to catch conversion errors */
	public static final byte[] LUAC_TAIL = { (byte) 0x19, (byte) 0x93, '\r', '\n', (byte) 0x1a, '\n', };


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
	private boolean luacLittleEndian;
	private int     luacSizeofInt;
	private int     luacSizeofSizeT;
	private int     luacSizeofInstruction;
	private int     luacSizeofLuaNumber;
	private int 	luacNumberFormat;

	/** input stream from which we are loading */
	public final DataInputStream is;

	/** Name of what is being loaded? */
	String name;

	private static final Object[]     NOVALUES    = {};
	private static final Prototype[] NOPROTOS    = {};
	private static final LocalVariable[]   NOLOCVARS   = {};
	private static final String[]  NOSTRVALUES = {};
	private static final Upvalue.Desc[]  NOUPVALDESCS = {};
	private static final int[]       NOINTS      = {};

	int bytesToInt(byte a, byte b, byte c, byte d) {
		return luacLittleEndian
				? (d << 24) | ((0xff & c) << 16) | ((0xff & b) << 8) | (0xff & a)
				: (a << 24) | ((0xff & b) << 16) | ((0xff & c) << 8) | (0xff & d);
	}

	/**
	 * Load a signed 32-bit integer from the input stream.
	 *
	 * @return the int value loaded.
	 */
	int loadInt32() throws IOException {
		byte[] buf = new byte[4];
		is.readFully(buf, 0, 4);
		return bytesToInt(buf[0], buf[1], buf[2], buf[3]);
	}

	/**
	 * Load a signed 64-bit integer from the input stream.
	 *
	 * @return the long value loaded.
	 */
	long loadInt64() throws IOException {
		int u = loadInt32();
		int v = loadInt32();

		long a, b;
		if (luacLittleEndian) {
			a = u;
			b = v;
		}
		else {
			b = u;
			a = v;
		}

		return (b << 32) | (a & 0xffffffffL);
	}

	/**
	 * Load an array of signed 32-bit integers from the input stream.
	 *
	 * @return the array of int values loaded.
	 */
	IntVector loadIntVector() throws IOException {
		int n = loadInt32();
		if (n == 0) {
			return IntVector.EMPTY;
		}

		// read all data at once
		int m = n << 2;
		byte[] buf = new byte[m];
		is.readFully(buf, 0, m);

		int[] array = new int[n];
		for (int i = 0; i < n; i++) {
			int j = i << 2;
			array[i] = bytesToInt(buf[j + 0], buf[j + 1], buf[j + 2], buf[j + 3]);
		}

		return IntVector.wrap(array);
	}

	boolean loadBoolean() throws IOException {
		return is.readUnsignedByte() != 0;
	}

	/** Load a string from the input stream.
	 *
	 * @return the string value loaded.
	 */
	String loadString() throws IOException {
		int size = this.luacSizeofSizeT == 8 ? (int) loadInt64() : loadInt32();
		if (size == 0) {
			return null;
		}

		byte[] bytes = new byte[size];
		is.readFully(bytes, 0, size);

		assert (bytes[size - 1] == 0);  // we can ignore the last byte

		char[] chars = new char[size - 1];
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

	public Object loadConstant() throws IOException {
		byte tag = is.readByte();
		switch (tag) {
			case LUA_TNIL:     return null;
			case LUA_TBOOLEAN: return loadBoolean();

			case LUA_TNUMINT:  return loadInteger();
			case LUA_TNUMFLT:  return loadFloat();

			case LUA_TSHRSTR:  return loadString();
			case LUA_TLNGSTR:  return loadString();  // TODO: is this correct?

			default: throw new IllegalStateException("Illegal constant type: " + tag);
		}
	}

	/**
	 * Load a list of constants from a binary chunk
	 * @param f the function prototype
	 * @throws IOException if an i/o exception occurs
	 */
	void loadConstants(Prototype.Builder f) throws IOException {
		int n = loadInt32();
		for (int i = 0; i < n; i++) {
			f.constants.add(loadConstant());
		}
	}

	void loadNestedPrototypes(Prototype.Builder f) throws IOException {
		int n = loadInt32();
		for (int i = 0; i < n; i++) {
			f.p.add(loadFunction(f.source));
		}
	}

	void loadUpvalues(Prototype.Builder f) throws IOException {
		int n = loadInt32();
		for (int i = 0; i < n; i++) {
			boolean instack = loadBoolean();
			int idx = ((int) is.readByte()) & 0xff;
			f.upvalues.add(new Upvalue.Desc.Builder(null, instack, idx));
		}
	}

	/**
	 * Load the debug info for a function prototype
	 * @param f the function Prototype
	 * @throws IOException if there is an i/o exception
	 */
	void loadDebugInfo(Prototype.Builder f) throws IOException {
		f.source = loadString();
		f.lineinfo = loadIntVector();

		int n = loadInt32();
		for (int i = 0; i < n; i++) {
			String varname = loadString();
			int startpc = loadInt32();
			int endpc = loadInt32();
			f.locvars.add(new LocalVariable(varname, startpc, endpc));
		}

		n = loadInt32();
		for (int i = 0; i < n; i++) {
			f.upvalues.get(i).name = loadString();
		}
	}

	/**
	 * Load a function prototype from the input stream
	 * @param p name of the source
	 * @return {@link Prototype} instance that was loaded
	 * @throws IOException
	 */
	public Prototype loadFunction(String p) throws IOException {
		Prototype.Builder f = new Prototype.Builder();
////		this.L.push(f);
//		f.source = loadString();
//		if ( f.source == null )
//			f.source = p;
		f.linedefined = loadInt32();
		f.lastlinedefined = loadInt32();
		f.numparams = is.readUnsignedByte();
		f.is_vararg = loadBoolean();
		f.maxstacksize = is.readUnsignedByte();
		f.code = loadIntVector();
		loadConstants(f);
		loadNestedPrototypes(f);
		loadUpvalues(f);
		loadDebugInfo(f);  // TODO: add support for debug-stripped chunks

		// TODO: add check here, for debugging purposes, I believe
		// see ldebug.c
//		 IF (!luaG_checkcode(f), "bad code");

//		 this.L.pop();
		 return f.build();
	}

	/**
	 * Load the lua chunk header values.
	 * @throws IOException if an i/o exception occurs.
	 */
	public void loadHeader() throws IOException {
		luacVersion = is.readByte();
		luacFormat = is.readByte();
		luacLittleEndian = (0 != is.readByte());
		luacSizeofInt = is.readByte();
		luacSizeofSizeT = is.readByte();
		luacSizeofInstruction = is.readByte();
		luacSizeofLuaNumber = is.readByte();
		luacNumberFormat = is.readByte();
		for (int i=0; i < LUAC_TAIL.length; ++i)
			if (is.readByte() != LUAC_TAIL[i])
				throw new RuntimeException("Unexpeted byte in luac tail of header, index="+i);
	}

	/**
	 * Load input stream as a lua binary chunk if the first 4 bytes are the lua binary signature.
	 * @param stream InputStream to read, after having read the first byte already
	 * @param chunkName Name to apply to the loaded chunk
	 * @return {@link Prototype} that was loaded, or null if the first 4 bytes were not the lua signature.
	 * @throws IOException if an IOException occurs
	 */
	public Prototype undump(InputStream stream, String chunkName) throws IOException {

		// check rest of signature
		if (stream.read() != LUA_SIGNATURE[0]
				|| stream.read() != LUA_SIGNATURE[1]
				|| stream.read() != LUA_SIGNATURE[2]
				|| stream.read() != LUA_SIGNATURE[3]) {
			return null;
		}

		// load file as a compiled chunk
		String sourceName = getSourceName(chunkName);
		PrototypeLoader s = new PrototypeLoader(stream, sourceName);
		s.loadHeader();

		// check format
		switch (s.luacNumberFormat) {
			case NUMBER_FORMAT_FLOATS_OR_DOUBLES:
			case NUMBER_FORMAT_INTS_ONLY:
			case NUMBER_FORMAT_NUM_PATCH_INT32:
				break;
			default:
				throw new CompatibilityException("unsupported int size");
		}
		return s.loadFunction(sourceName);
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

	/** Private constructor for create a load state */
	private PrototypeLoader(InputStream stream, String name) {
		this.name = name;
		this.is = new DataInputStream(stream);
	}

	private PrototypeLoader() {
		this.name = "";
		this.is = null;
	}
}
