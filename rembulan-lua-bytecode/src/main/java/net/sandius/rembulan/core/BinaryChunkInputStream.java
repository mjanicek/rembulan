package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Objects;

public class BinaryChunkInputStream extends FilterInputStream {

	protected final ByteOrder byteOrder;

	protected final int sizeOfInt;
	protected final int sizeOfSizeT;
	protected final int sizeOfInstruction;
	protected final int sizeOfLuaInteger;
	protected final int sizeOfLuaFloat;

	public BinaryChunkInputStream(InputStream in, ByteOrder byteOrder, int sizeOfInt, int sizeOfSizeT, int sizeOfInstruction, int sizeOfLuaInteger, int sizeOfLuaFloat) {
		super(in);

		this.byteOrder = Objects.requireNonNull(byteOrder);
		this.sizeOfInt = sizeOfInt;
		this.sizeOfSizeT = sizeOfSizeT;
		this.sizeOfInstruction = sizeOfInstruction;
		this.sizeOfLuaInteger = sizeOfLuaInteger;
		this.sizeOfLuaFloat = sizeOfLuaFloat;
	}

	protected boolean isBigEndian() {
		return byteOrder == ByteOrder.BIG_ENDIAN;
	}

	protected int readUnsignedByte() throws IOException {
		int c = in.read();
		if (c < 0) throw new EOFException();
		return c;
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
		if (len < 0) throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = in.read(b, off + n, len - n);
			if (count < 0) throw new EOFException();
			n += count;
		}
	}

	public boolean readBoolean() throws IOException {
		return readUnsignedByte() != 0;
	}

	protected int readInt32() throws IOException {
		int c1 = in.read();
		int c2 = in.read();
		int c3 = in.read();
		int c4 = in.read();

		if ((c1 | c2 | c3 | c4) < 0) throw new EOFException();

		return isBigEndian()
				? ((c1 << 24) + (c2 << 16) + (c3 << 8) + (c4 << 0))
				: ((c1 << 0) + (c2 << 8) + (c3 << 16) + (c4 << 24));
	}

	protected long readInt64() throws IOException {
		int a = readInt32();
		int b = readInt32();

		return isBigEndian()
				? (long) (a << 32) + (long) (b << 0)
				: (long) (a << 0) + (long) (b << 32);
	}

	public int readInt() throws IOException {
		if (sizeOfInt == 4) {
			return readInt32();
		}
		else if (sizeOfInt == 8) {
			long l = readInt64();
			if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
				return (int) l;
			}
			else {
				throw new IllegalArgumentException("64-bit int cannot be represented in 32-bit: " + l);
			}
		}
		else {
			throw new IllegalArgumentException("Illegal size of int: " + sizeOfInt);
		}
	}

	public int readSizeT() throws IOException  {
		if (sizeOfSizeT == 4) {
			return readInt32();
		}
		else if (sizeOfSizeT == 8) {
			long l = readInt64();
			if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
				return (int) l;
			}
			else {
				throw new IllegalArgumentException("64-bit size_t cannot be represented in 32-bit: " + l);
			}
		}
		else {
			throw new IllegalArgumentException("Illegal size of size_t: " + sizeOfSizeT);
		}
	}

	public long readInteger() throws IOException  {
		if (sizeOfLuaInteger == 8) {
			return readInt64();
		}
		else if (sizeOfLuaInteger == 4) {
			return readInt32();
		}
		else {
			throw new IllegalArgumentException("Illegal size of Lua integer: " + sizeOfLuaInteger);
		}
	}

	public double readFloat() throws IOException  {
		if (sizeOfLuaFloat == 8) {
			return Double.longBitsToDouble(readInt64());
		}
		else if (sizeOfLuaFloat == 4) {
			return Double.longBitsToDouble(readInt32());
		}
		else {
			throw new IllegalArgumentException("Illegal size of Lua float: " + sizeOfLuaInteger);
		}
	}

	public String readString() throws IOException  {
		int hx = readUnsignedByte();
		return hx == 0xff ? readLongString() : readShortStringBody(hx);
	}

	protected String readShortStringBody(int length) throws IOException {
		Check.inRange(length, 0, 0xff - 1);
		if (length == 0) return null;  // FIXME: null or "" ?
		else return readStringBody(new byte[length - 1]);
	}

	public String readShortString() throws IOException {
		int length = readUnsignedByte();
		return readShortStringBody(length);
	}

	public String readLongString() throws IOException {
		int length = readSizeT();
		Check.gt(length, 0xff);
		return readStringBody(new byte[length - 1]);
	}

	protected String readStringBody(byte[] bytes) throws IOException {
		readFully(bytes, 0, bytes.length);

		char[] chars = new char[bytes.length];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) (bytes[i] & 0xff);
		}

		return String.valueOf(chars);
	}

	/**
	 * Load an array of signed 32-bit integers from the input stream.
	 *
	 * @return the array of int values loaded.
	 */
	public int[] readIntArray() throws IOException {
		int n = readInt();
		int[] array = new int[n];
		for (int i = 0; i < n; i++) {
			array[i] = readInt();
		}
		return array;
	}

	private static final byte[] HEADER_SIGNATURE = { '\033', 'L', 'u', 'a' };
	public static final int HEADER_VERSION = 0x53;
	public static final int HEADER_FORMAT = 0;
	private static final byte[] HEADER_TAIL = { (byte) 0x19, (byte) 0x93, '\r', '\n', (byte) 0x1a, '\n', };

	public static final long TestInteger = 0x5678L;
	public static final double TestFloat = 370.5;

	// returns -1 if matches, index of first non-matching byte otherwise
	private static int readBinaryLiteral(DataInputStream stream, byte[] expected) throws IOException {
		for (int i = 0; i < expected.length; i++) {
			int b = stream.readUnsignedByte();
			if ((byte) b != expected[i]) return i;
		}
		return -1;
	}

	private static void readAndCheckLuaSignature(DataInputStream stream) throws IOException {
		{
			int diff = readBinaryLiteral(stream, HEADER_SIGNATURE);
			if (diff != -1) {
				throw new IllegalArgumentException("Unexpected byte at position " + diff);
			}
		}

		int version = stream.readUnsignedByte();
		if (version != HEADER_VERSION) throw new IllegalArgumentException("Unsupported version: " + Integer.toHexString(version));

		int format = stream.readUnsignedByte();
		if (format != HEADER_FORMAT) throw new IllegalArgumentException("Unsupported format: " + Integer.toHexString(format));

		{
			int diff = readBinaryLiteral(stream, HEADER_TAIL);
			if (diff != -1) {
				throw new IllegalArgumentException("Unexpected byte at position " + (HEADER_SIGNATURE.length + 2 + diff));
			}
		}
	}

	protected static ByteOrder testByteOrder(long actualValue, long bigEndianValue) {
		if (actualValue == bigEndianValue) return ByteOrder.BIG_ENDIAN;
		else if (actualValue == Long.reverseBytes(bigEndianValue)) return ByteOrder.LITTLE_ENDIAN;
		else return null;
	}

	protected static ByteOrder decideByteOrder(long integerBits, long floatBits) {
		ByteOrder integerByteOrder = testByteOrder(integerBits, TestInteger);
		ByteOrder floatByteOrder = testByteOrder(floatBits, Double.doubleToLongBits(TestFloat));

		if (integerByteOrder != null && floatByteOrder != null) {
			if (integerByteOrder != floatByteOrder) {
				throw new IllegalArgumentException("Byte order mismatch: " + integerByteOrder + " (ints) vs " + floatByteOrder + " (floats)");
			}
			else {
				return integerByteOrder;
			}
		}
		else if (integerByteOrder != null) {
			return integerByteOrder;
		}
		else if (floatByteOrder != null) {
			return floatByteOrder;
		}
		else {
			throw new IllegalArgumentException("Unable to determine byte order: 0x" + Long.toHexString(integerBits) + " (integer as big-endian long),  0x" + Long.toHexString(floatBits) + " (float as big-endian long)");
		}
	}

	public static BinaryChunkInputStream fromInputStream(InputStream stream) throws IOException {
		DataInputStream dis = new DataInputStream(stream);
		readAndCheckLuaSignature(dis);

		int sizeOfInt = dis.readUnsignedByte();
		int sizeOfSizeT = dis.readUnsignedByte();
		int sizeOfInstruction = dis.readUnsignedByte();
		int sizeOfLuaInteger = dis.readUnsignedByte();
		int sizeOfLuaFloat = dis.readUnsignedByte();

		// detect endianness
        long ti = dis.readLong();
		long tf = dis.readLong();
		ByteOrder byteOrder = decideByteOrder(ti, tf);

		boolean isFunction = dis.readBoolean();
		if (!isFunction) {
			throw new IllegalArgumentException("Function expected");
		}

		return new BinaryChunkInputStream(stream, byteOrder, sizeOfInt, sizeOfSizeT, sizeOfInstruction, sizeOfLuaInteger, sizeOfLuaFloat);
	}

}
