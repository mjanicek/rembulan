package net.sandius.rembulan.core.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.ByteOrder;

public abstract class ByteArrayDataOutputStream extends ByteArrayOutputStream implements DataOutput {

	protected ByteArrayDataOutputStream(int size) {
		super(size);
	}

	protected ByteArrayDataOutputStream() {
		super();
	}

	public static ByteArrayDataOutputStream newInstance(ByteOrder byteOrder, int size) {
		return byteOrder == ByteOrder.BIG_ENDIAN
				? new BigEndianByteArrayDataOutputStream(size)
				: new LittleEndianByteArrayDataOutputStream(size);
	}

	public static ByteArrayDataOutputStream newInstance(ByteOrder byteOrder) {
		return byteOrder == ByteOrder.BIG_ENDIAN
				? new BigEndianByteArrayDataOutputStream()
				: new LittleEndianByteArrayDataOutputStream();
	}

	public static ByteArrayDataOutputStream newInstance(int size) {
		return new BigEndianByteArrayDataOutputStream(size);
	}

	public static ByteArrayDataOutputStream newInstance() {
		return new BigEndianByteArrayDataOutputStream();
	}

	public abstract ByteOrder byteOrder();

	public boolean isBigEndian() {
		return (byteOrder() == ByteOrder.BIG_ENDIAN);
	}

	public boolean isLittleEndian() {
		return (byteOrder() == ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		write(v ? 1 : 0);
	}

	@Override
	public void writeByte(int v) throws IOException {
		write(v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		int len = s.length();
		for (int i = 0 ; i < len ; i++) {
			write((byte) s.charAt(i));
		}
	}

	@Override
	public void writeChars(String s) throws IOException {
		int len = s.length();
		for (int i = 0 ; i < len ; i++) {
			int v = s.charAt(i);
			writeChar(v);
		}
	}

	@Override
	public void writeFloat(float v) throws IOException {
		writeInt(Float.floatToIntBits(v));
	}

	@Override
	public void writeDouble(double v) throws IOException {
		writeLong(Double.doubleToLongBits(v));
	}

	@Override
	public void writeUTF(String s) throws IOException {
		writeUTF(s, isBigEndian(), this);
	}

	/**
	 * Writes a string to the specified DataOutput using
	 * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
	 * encoding in a machine-independent manner.
	 * <p>
	 * First, two bytes are written to out as if by the <code>writeShort</code>
	 * method giving the number of bytes to follow. This value is the number of
	 * bytes actually written out, not the length of the string. Following the
	 * length, each character of the string is output, in sequence, using the
	 * modified UTF-8 encoding for the character. If no exception is thrown, the
	 * counter <code>written</code> is incremented by the total number of
	 * bytes written to the output stream. This will be at least two
	 * plus the length of <code>str</code>, and at most two plus
	 * thrice the length of <code>str</code>.
	 *
	 * @param      str   a string to be written.
	 * @param      out   destination to write to
	 * @return     The number of bytes written out.
	 * @exception  IOException  if an I/O error occurs.
	 */
	// taken from static java.io.DataOutputStream.writeUTF(String, DataOutput)
	static int writeUTF(String str, boolean bigEndian, DataOutput out) throws IOException {
		int strlen = str.length();
		int utflen = 0;
		int c, count = 0;

		/* use charAt instead of copying String to char array */
		for (int i = 0; i < strlen; i++) {
			c = str.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				utflen++;
			}
			else if (c > 0x07FF) {
				utflen += 3;
			}
			else {
				utflen += 2;
			}
		}

		if (utflen > 65535)
			throw new UTFDataFormatException(
					"encoded string too long: " + utflen + " bytes");

		byte[] bytearr = new byte[utflen + 2];

		if (bigEndian) {
			bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
			bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);
		}
		else {
			bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);
			bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
		}

		int i = 0;
		for (i = 0; i < strlen; i++) {
			c = str.charAt(i);
			if (!((c >= 0x0001) && (c <= 0x007F))) break;
			bytearr[count++] = (byte) c;
		}

		for (; i < strlen; i++) {
			c = str.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				bytearr[count++] = (byte) c;

			}
			else if (c > 0x07FF) {
				bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
				bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
				bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			}
			else {
				bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
				bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			}
		}
		out.write(bytearr, 0, utflen + 2);
		return utflen + 2;
	}

}
