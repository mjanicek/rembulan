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

	/** The character encoding to use for file encoding.  Null means the default encoding */
	public static String encoding = null;

	/** Signature byte indicating the file is a compiled binary chunk */
	public static final byte[] LUA_SIGNATURE	= { '\033', 'L', 'u', 'a' };

	/** Data to catch conversion errors */
	public static final byte[] LUAC_TAIL = { (byte) 0x19, (byte) 0x93, '\r', '\n', (byte) 0x1a, '\n', };


	/** Name for compiled chunks */
	public static final String SOURCE_BINARY_STRING = "binary string";


	/** for header of binary files -- this is Lua 5.2 */
	public static final int LUAC_VERSION		= 0x52;

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

	/** Read buffer */
	private byte[] buf = new byte[512];

	/** Load a 4-byte int value from the input stream
	 * @return the int value loaded.
	 **/
	int loadInt() throws IOException {
		is.readFully(buf,0,4);
		return luacLittleEndian?
				(buf[3] << 24) | ((0xff & buf[2]) << 16) | ((0xff & buf[1]) << 8) | (0xff & buf[0]):
				(buf[0] << 24) | ((0xff & buf[1]) << 16) | ((0xff & buf[2]) << 8) | (0xff & buf[3]);
	}

	/** Load an array of int values from the input stream
	 * @return the array of int values loaded.
	 **/
	int[] loadIntArray() throws IOException {
		int n = loadInt();
		if ( n == 0 )
			return NOINTS;

		// read all data at once
		int m = n << 2;
		if ( buf.length < m )
			buf = new byte[m];
		is.readFully(buf,0,m);
		int[] array = new int[n];
		for ( int i=0, j=0; i<n; ++i, j+=4 )
			array[i] = luacLittleEndian?
					(buf[j+3] << 24) | ((0xff & buf[j+2]) << 16) | ((0xff & buf[j+1]) << 8) | (0xff & buf[j+0]):
					(buf[j+0] << 24) | ((0xff & buf[j+1]) << 16) | ((0xff & buf[j+2]) << 8) | (0xff & buf[j+3]);

		return array;
	}

	/** Load a long  value from the input stream
	 * @return the long value loaded.
	 **/
	long loadInt64() throws IOException {
		int a,b;
		if ( this.luacLittleEndian ) {
			a = loadInt();
			b = loadInt();
		} else {
			b = loadInt();
			a = loadInt();
		}
		return (((long)b)<<32) | (((long)a)&0xffffffffL);
	}

	/** Load a lua string value from the input stream.
	 * @return the string value loaded.
	 **/
	String loadString() throws IOException {
		int size = this.luacSizeofSizeT == 8 ? (int) loadInt64() : loadInt();
		if (size == 0) {
			return null;
		}

		byte[] bytes = new byte[size];
		is.readFully(bytes, 0, size);

		char[] chars = new char[size - 1];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) (bytes[i] & 0xff);
		}

		return String.valueOf(chars);
	}

	/**
	 * Convert bits in a long value to a number.
	 * @param bits long value containing the bits
	 * @return {@link Long} or {@link Double} whose value corresponds to the bits provided.
	 */
	public static Number longBitsToLuaNumber( long bits ) {
		if ( ( bits & ( ( 1L << 63 ) - 1 ) ) == 0L ) {
			return 0L;
		}

		int e = (int)((bits >> 52) & 0x7ffL) - 1023;

		if ( e >= 0 && e < 31 ) {
			long f = bits & 0xFFFFFFFFFFFFFL;
			int shift = 52 - e;
			long intPrecMask = ( 1L << shift ) - 1;
			if ( ( f & intPrecMask ) == 0 ) {
				int intValue = (int)( f >> shift ) | ( 1 << e );
				return Long.valueOf( ( ( bits >> 63 ) != 0 ) ? -intValue : intValue );
			}
		}

		return Double.longBitsToDouble(bits);
	}

	/**
	 * Load a number from a binary chunk
	 * @return the {@link Number} loaded
	 * @throws IOException if an i/o exception occurs
	 */
	Number loadNumber() throws IOException {
		if ( luacNumberFormat == NUMBER_FORMAT_INTS_ONLY ) {
			return loadInt();
		} else {
			return longBitsToLuaNumber( loadInt64() );
		}
	}

	/**
	 * Load a list of constants from a binary chunk
	 * @param f the function prototype
	 * @throws IOException if an i/o exception occurs
	 */
	void loadConstants(Prototype.Builder f) throws IOException {
		int n = loadInt();

		for (int i = 0; i < n; i++) {
			Object v;

			switch (is.readByte()) {
				case LUA_TNIL:
					v = null;
					break;
				case LUA_TBOOLEAN:
					v = (0 != is.readUnsignedByte() ? Boolean.TRUE : Boolean.FALSE);
					break;
				case LUA_TINT:
					v = Integer.valueOf(loadInt());
					break;
				case LUA_TNUMBER:
					v = loadNumber();
					break;
				case LUA_TSTRING:
					v = loadString();
					break;
				default:
					throw new IllegalStateException("bad constant");
			}

			f.constants.add(v);
		}

		n = loadInt();
		Prototype[] protos = n > 0 ? new Prototype[n] : NOPROTOS;
		for (int i = 0; i < n; i++)
			protos[i] = loadFunction(f.source);

		f.p.clear();
		for (Prototype proto : protos) {
			f.p.add(new Prototype.Builder(proto));
		}
	}


	void loadUpvalues(Prototype.Builder f) throws IOException {
		int n = loadInt();
		for (int i = 0; i < n; i++) {
			boolean instack = is.readByte() != 0;
			int idx = ((int) is.readByte()) & 0xff;
			f.upvalues.add(new Upvalue.Desc.Builder(null, instack, idx));
		}
	}

	/**
	 * Load the debug info for a function prototype
	 * @param f the function Prototype
	 * @throws IOException if there is an i/o exception
	 */
	void loadDebug(Prototype.Builder f) throws IOException {
		f.source = loadString();
		f.lineinfo.set(loadIntArray());

		int n = loadInt();
		for (int i = 0; i < n; i++) {
			String varname = loadString();
			int startpc = loadInt();
			int endpc = loadInt();
			f.locvars.add(new LocalVariable.Builder(varname, startpc, endpc));
		}

		n = loadInt();
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
		f.linedefined = loadInt();
		f.lastlinedefined = loadInt();
		f.numparams = is.readUnsignedByte();
		f.is_vararg = is.readUnsignedByte();
		f.maxstacksize = is.readUnsignedByte();
		f.code.set(loadIntArray());
		loadConstants(f);
		loadUpvalues(f);
		loadDebug(f);

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
	 * @param chunkname Name to apply to the loaded chunk
	 * @return {@link Prototype} that was loaded, or null if the first 4 bytes were not the lua signature.
	 * @throws IOException if an IOException occurs
	 */
	public Prototype undump(InputStream stream, String chunkname) throws IOException {

		// check rest of signature
		if (stream.read() != LUA_SIGNATURE[0]
				|| stream.read() != LUA_SIGNATURE[1]
				|| stream.read() != LUA_SIGNATURE[2]
				|| stream.read() != LUA_SIGNATURE[3]) {
			return null;
		}

		// load file as a compiled chunk
		String sname = getSourceName(chunkname);
		PrototypeLoader s = new PrototypeLoader(stream, sname);
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
		return s.loadFunction(sname);
	}

	/**
	 * Construct a source name from a supplied chunk name
	 * @param name String name that appears in the chunk
	 * @return source file name
	 */
	public static String getSourceName(String name) {
		String sname = name;
		if (name.startsWith("@") || name.startsWith("=")) sname = name.substring(1);
		else if (name.startsWith("\033")) sname = SOURCE_BINARY_STRING;
		return sname;
	}

	/** Private constructor for create a load state */
	private PrototypeLoader(InputStream stream, String name) {
		this.name = name;
		this.is = new DataInputStream( stream );
	}

	private PrototypeLoader() {
		this.name = "";
		this.is = null;
	}
}
