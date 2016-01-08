package net.sandius.rembulan.core.util;

import java.io.IOException;
import java.nio.ByteOrder;

public class LittleEndianByteArrayDataOutputStream extends ByteArrayDataOutputStream {

	public LittleEndianByteArrayDataOutputStream(int size) {
		super(size);
	}

	public LittleEndianByteArrayDataOutputStream() {
		super();
	}

	@Override
	public final ByteOrder byteOrder() {
		return ByteOrder.LITTLE_ENDIAN;
	}

	@Override
	public void writeShort(int v) throws IOException {
		write((v >>> 0) & 0xFF);
		write((v >>> 8) & 0xFF);
	}

	@Override
	public void writeChar(int v) throws IOException {
		write((v >>> 0) & 0xFF);
		write((v >>> 8) & 0xFF);
	}

	@Override
	public void writeInt(int v) throws IOException {
		write((v >>>  0) & 0xFF);
		write((v >>>  8) & 0xFF);
		write((v >>> 16) & 0xFF);
		write((v >>> 24) & 0xFF);
	}

	@Override
	public void writeLong(long v) throws IOException {
		byte[] buf = new byte[8];

		buf[0] = (byte)(v >>>  0);
		buf[1] = (byte)(v >>>  8);
		buf[2] = (byte)(v >>> 16);
		buf[3] = (byte)(v >>> 24);
		buf[4] = (byte)(v >>> 32);
		buf[5] = (byte)(v >>> 40);
		buf[6] = (byte)(v >>> 48);
		buf[7] = (byte)(v >>> 56);

		write(buf, 0, 8);
	}

}
