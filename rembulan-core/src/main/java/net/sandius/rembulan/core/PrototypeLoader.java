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
	private boolean luacLittleEndian;
	private int     luacSizeofInt;
	private int     luacSizeofSizeT;
	private int     luacSizeofInstruction;
	private int     luacSizeofLuaInteger;
	private int     luacSizeofLuaNumber;

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
		System.err.println("  32-bit to int: ["
				+ "0x" + Integer.toHexString(a & 0xff)
				+ " "
				+ "0x" + Integer.toHexString(b & 0xff)
				+ " "
				+ "0x" + Integer.toHexString(c & 0xff)
				+ " "
				+ "0x" + Integer.toHexString(d & 0xff)
				+ "]");

		return luacLittleEndian
				? ((0xff & d) << 24) | ((0xff & c) << 16) | ((0xff & b) << 8) | (0xff & a)
				: ((0xff & a) << 24) | ((0xff & b) << 16) | ((0xff & c) << 8) | (0xff & d);
	}

	/**
	 * Load a signed 32-bit integer from the input stream.
	 *
	 * @return the int value loaded.
	 */
	int loadInt32() throws IOException {
		byte[] buf = new byte[4];
		is.readFully(buf, 0, 4);
		int i = bytesToInt(buf[0], buf[1], buf[2], buf[3]);
		System.err.println("  loadInt32: " + i);
		return i;
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

		System.err.println("  loadIntVector of length " + n);

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
			System.err.println("  int[" + i + "] == " + array[i]);

			int insn = array[i];
			System.err.println("    code? opcode=" + OpCode.opCode(insn)
					+ " a=" + OpCode.arg_A(insn)
					+ " b=" + OpCode.arg_B(insn)
					+ " c=" + OpCode.arg_C(insn)
					+ " ax=" + OpCode.arg_Ax(insn)
					+ " bx=" + OpCode.arg_Bx(insn)
					+ " sbx=" + OpCode.arg_sBx(insn));
		}

		return IntVector.wrap(array);
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

		System.err.println("  loadString of length " + size);

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

		String s = String.valueOf(chars);

		System.err.println("  loadString yields \"" + s + "\"");

		return s;
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

		System.err.println("num of nested prototypes: " + n);

		for (int i = 0; i < n; i++) {
			f.p.add(loadFunction(f.source));
		}
	}

	void loadUpvalues(Prototype.Builder f) throws IOException {
		int n = loadInt32();

		System.err.println("num of upvalues: " + n);

		for (int i = 0; i < n; i++) {
			boolean instack = loadBoolean();
			int idx = ((int) is.readByte()) & 0xff;
			f.upvalues.add(new Upvalue.Desc.Builder(null, instack, idx));
			System.err.println("  upval #" + i + ": instack=" + (instack ? "true" : "false") + ", idx=" + idx);
		}
	}

	/**
	 * Load the debug info for a function prototype
	 * @param f the function Prototype
	 * @throws IOException if there is an i/o exception
	 */
	void loadDebugInfo(Prototype.Builder f) throws IOException {
		f.lineinfo = loadIntVector();

		System.err.println("num of lineinfos: " + f.lineinfo.length());

		int n = loadInt32();

		System.err.println("num of locvars: " + n);

		for (int i = 0; i < n; i++) {
			String varname = loadString();
			int startpc = loadInt32();
			int endpc = loadInt32();
			System.err.println("  locvar #" + i + ": \"" + varname + "\" [" + startpc + ", " + endpc + "]");
			f.locvars.add(new LocalVariable(varname, startpc, endpc));
		}

		n = loadInt32();

		System.err.println("num of upvalue names: " + n);

		for (int i = 0; i < n; i++) {
			f.upvalues.get(i).name = loadString();
			System.err.println("  upval #" + i + " name: \"" + f.upvalues.get(i).name + "\"");
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

		boolean isClosure = loadBoolean();
		// TODO: require true
		System.err.println("isClosure: " + (isClosure ? "true" : "false"));


////		this.L.push(f);
		f.source = loadString();
		if (f.source == null) f.source = p;
		System.err.println("source: " + f.source);

		f.linedefined = loadInt32();
		System.err.println("linedefined: " + f.linedefined);
		f.lastlinedefined = loadInt32();
		System.err.println("lastlinedefined: " + f.lastlinedefined);
		f.numparams = is.readUnsignedByte();
		System.err.println("numparams: " + f.numparams);
		f.is_vararg = loadBoolean();
		System.err.println("is_vararg: " + (f.is_vararg ? "true" : "false"));
		f.maxstacksize = is.readUnsignedByte();
		System.err.println("maxstacksize: " + f.maxstacksize);

		f.code = loadIntVector();

		System.err.println("code is int array of length " + f.code.length());

		loadConstants(f);

		System.err.println("constants #: " + f.constants.size());

		loadUpvalues(f);
		loadNestedPrototypes(f);
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

		for (int i = 0; i < LUAC_TAIL.length; ++i) {
			if (is.readByte() != LUAC_TAIL[i]) throw new RuntimeException("Unexpected byte in LuaC tail of header, index=" + i);
		}

		luacSizeofInt = is.readByte();
		System.err.println("sizeofint: " + luacSizeofInt);
		luacSizeofSizeT = is.readByte();
		System.err.println("sizeofsize_t: " + luacSizeofSizeT);
		luacSizeofInstruction = is.readByte();
		System.err.println("sizeofinstruction: " + luacSizeofInstruction);
		luacSizeofLuaInteger = is.readByte();
		System.err.println("sizeofluainteger: " + luacSizeofLuaInteger);
		luacSizeofLuaNumber = is.readByte();
		System.err.println("sizeofluanumber: " + luacSizeofLuaNumber);

		// check endianness
        long ti = is.readLong();

		System.err.println("endian-int: " + Long.toHexString(ti));

		if ((ti == TestIntLE) == (ti == TestIntBE)) {
			throw new IllegalArgumentException("Endianness mismatch: 0x" + Long.toHexString(ti));
        }
        else {
			luacLittleEndian = (ti == TestIntLE);
        }

		System.err.println("little endian: " + (luacLittleEndian ? "true" : "false"));

        // TODO: use loadNumber here!
        long tn = is.readLong();

		System.err.println("endian-num: " + Long.toHexString(tn));

		if (luacLittleEndian) {
			// TODO: this!
        }
        else {
			// no conversion necessary
			// TODO
//			Double.longBitsToDouble(tn);
        }
	}

	/**
	 * Load input stream as a lua binary chunk if the first 4 bytes are the lua binary signature.
	 * @param stream InputStream to read, after having read the first byte already
	 * @param chunkName Name to apply to the loaded chunk
	 * @return {@link Prototype} that was loaded, or null if the first 4 bytes were not the lua signature.
	 * @throws IOException if an IOException occurs
	 */
	public static Prototype undump(InputStream stream, String chunkName, boolean littleEndian) throws IOException {

		// check rest of signature
		if (stream.read() != LUA_SIGNATURE[0]
				|| stream.read() != LUA_SIGNATURE[1]
				|| stream.read() != LUA_SIGNATURE[2]
				|| stream.read() != LUA_SIGNATURE[3]) {
			return null;
		}

		// load file as a compiled chunk
		String sourceName = getSourceName(chunkName);
		PrototypeLoader s = new PrototypeLoader(stream, sourceName, littleEndian);
		s.loadHeader();

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
	// TODO: endianness not needed?
	public PrototypeLoader(InputStream stream, String name, boolean littleEndian) {
		this.name = name;
		this.is = new DataInputStream(stream);
		this.luacLittleEndian = littleEndian;
	}

	private PrototypeLoader() {
		this.name = "";
		this.is = null;
	}
}
