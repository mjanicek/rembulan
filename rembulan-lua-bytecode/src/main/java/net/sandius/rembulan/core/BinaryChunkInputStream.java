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

	protected final boolean bigEndian;

	protected final boolean intIs32Bit;
	protected final boolean sizeTIs32Bit;
	protected final boolean instructionIs32Bit;
	protected final boolean luaIntegerIs32Bit;
	protected final boolean luaFloatIs32Bit;

	private static boolean bitWidthIs32Bit(int bitWidth) {
		if (bitWidth == 4) return true;
		else if (bitWidth == 8) return false;
		else throw new IllegalArgumentException("Illegal bit width: " + bitWidth + ", expected 4 or 8");
	}

	public BinaryChunkInputStream(InputStream in, ByteOrder byteOrder, int sizeOfInt, int sizeOfSizeT, int sizeOfInstruction, int sizeOfLuaInteger, int sizeOfLuaFloat) {
		super(in);

		this.bigEndian = Objects.requireNonNull(byteOrder) == ByteOrder.BIG_ENDIAN;

		this.intIs32Bit = bitWidthIs32Bit(sizeOfInt);
		this.sizeTIs32Bit = bitWidthIs32Bit(sizeOfSizeT);
		this.instructionIs32Bit = bitWidthIs32Bit(sizeOfInstruction);
		this.luaIntegerIs32Bit = bitWidthIs32Bit(sizeOfLuaInteger);
		this.luaFloatIs32Bit = bitWidthIs32Bit(sizeOfLuaFloat);
	}

	public BinaryChunkInputStream(InputStream in, BinaryChunkFormat format) {
		this(in, format.byteOrder, format.sizeOfInt, format.sizeOfSizeT, format.sizeOfInstruction, format.sizeOfLuaInteger, format.sizeOfLuaFloat);
	}

	public BinaryChunkFormat getFormat() {
		return new BinaryChunkFormat(
				bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN,
				intIs32Bit ? 4 : 8,
				sizeTIs32Bit ? 4 : 8,
				instructionIs32Bit ? 4 : 8,
				luaIntegerIs32Bit ? 4 : 8,
				luaFloatIs32Bit ? 4 : 8
		);
	}

	protected int readUnsignedByte() throws IOException {
		int c = read();
		if (c < 0) throw new EOFException();
		return c;
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
		if (len < 0) throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = read(b, off + n, len - n);
			if (count < 0) throw new EOFException();
			n += count;
		}
	}

	public boolean readBoolean() throws IOException {
		return readUnsignedByte() != 0;
	}

	protected int readInt32() throws IOException {
		int c1 = read();
		int c2 = read();
		int c3 = read();
		int c4 = read();

		if ((c1 | c2 | c3 | c4) < 0) throw new EOFException();

		return bigEndian
				? ((c1 << 24) + (c2 << 16) + (c3 << 8) + (c4 << 0))
				: ((c1 << 0) + (c2 << 8) + (c3 << 16) + (c4 << 24));
	}

	protected long readInt64() throws IOException {
		int a = readInt32();
		int b = readInt32();

		return bigEndian
				? (long) (a << 32) + (long) (b << 0)
				: (long) (a << 0) + (long) (b << 32);
	}

	private int readInt(boolean is32Bit) throws IOException {
		if (is32Bit) {
			return readInt32();
		}
		else {
			long l = readInt64();
			if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
				return (int) l;
			}
			else {
				throw new IllegalArgumentException("64-bit value cannot be represented in 32-bit: " + l);
			}
		}
	}

	public int readInt() throws IOException {
		return readInt(intIs32Bit);
	}

	public int readSizeT() throws IOException  {
		return readInt(sizeTIs32Bit);
	}

	public int readInstruction() throws IOException {
		return readInt(instructionIs32Bit);
	}

	public long readInteger() throws IOException  {
		if (luaIntegerIs32Bit) {
			return readInt32();
		}
		else {
			return readInt64();
		}
	}

	public double readFloat() throws IOException  {
		if (luaFloatIs32Bit) {
			return (double) Float.intBitsToFloat(readInt32());
		}
		else {
			return Double.longBitsToDouble(readInt64());
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

	// returns -1 if matches, index of first non-matching byte otherwise
	private static void readBinaryLiteral(DataInputStream stream, String expected, int offset) throws IOException {
		for (int i = 0; i < expected.length(); i++) {
			int b = stream.readUnsignedByte();
			int exp = expected.charAt(i) & 0xff;

			if (b != exp) {
				throw new IllegalArgumentException("Unexpected byte at position " + (offset + i)
						+ ": expecting 0x" + Integer.toHexString(exp)
						+ ", got 0x" + Integer.toHexString(b));
			}
		}
	}

	private static void readAndCheckLuaSignature(DataInputStream stream) throws IOException {
		readBinaryLiteral(stream, BinaryChunkConstants.SIGNATURE, 0);

		int version = stream.readUnsignedByte();
		if (version != BinaryChunkConstants.VERSION) throw new IllegalArgumentException("Unsupported version: " + Integer.toHexString(version));

		int format = stream.readUnsignedByte();
		if (format != BinaryChunkConstants.FORMAT) throw new IllegalArgumentException("Unsupported format: " + Integer.toHexString(format));

		readBinaryLiteral(stream, BinaryChunkConstants.TAIL, BinaryChunkConstants.SIGNATURE.length() + 2);
	}

	protected static ByteOrder testByteOrder(long actualValue, long bigEndianValue) {
		if (actualValue == bigEndianValue) return ByteOrder.BIG_ENDIAN;
		else if (actualValue == Long.reverseBytes(bigEndianValue)) return ByteOrder.LITTLE_ENDIAN;
		else return null;
	}

	protected static ByteOrder decideByteOrder(long integerBits, long floatBits) {
		ByteOrder integerByteOrder = testByteOrder(integerBits, BinaryChunkConstants.BYTE_ORDER_TEST_INTEGER);
		ByteOrder floatByteOrder = testByteOrder(floatBits, Double.doubleToLongBits(BinaryChunkConstants.BYTE_ORDER_TEST_FLOAT));

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
