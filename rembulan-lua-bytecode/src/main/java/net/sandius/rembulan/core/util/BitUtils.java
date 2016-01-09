package net.sandius.rembulan.core.util;

import java.nio.ByteOrder;

public class BitUtils {

	public static String byteToHex(int b) {
		String s = Integer.toHexString(b & 0xff);
		return s.length() == 1 ? "0" + s : s;
	}

	public static String toHexString(byte[] bytes) {
		StringBuilder bld = new StringBuilder();
		for (byte b : bytes) {
			bld.append(byteToHex(b));
		}
		return bld.toString();
	}

	public static ByteOrder testByteOrder(byte[] b, long testValue) {
		if (b.length == 4) {  // 32-bit case
			int i = ((b[0] & 0xff) << 24)
					+ ((b[1] & 0xff) << 16)
					+ ((b[2] & 0xff) << 8)
					+ ((b[3] & 0xff) << 0);

			if (i == (int) testValue) return ByteOrder.BIG_ENDIAN;
			else if (Integer.reverseBytes(i) == (int) testValue) return ByteOrder.LITTLE_ENDIAN;
			else return null;
		}
		else if (b.length == 8) {  // 64-bit case
			long l = ((long) b[0] << 56)
					+ ((long) (b[1] & 0xff) << 48)
					+ ((long) (b[2] & 0xff) << 40)
					+ ((long) (b[3] & 0xff) << 32)
					+ ((long) (b[4] & 0xff) << 24)
					+ ((b[5] & 0xff) << 16)
					+ ((b[6] & 0xff) <<  8)
					+ ((b[7] & 0xff) <<  0);

			if (l == testValue) return ByteOrder.BIG_ENDIAN;
			else if (Long.reverseBytes(l) == testValue) return ByteOrder.LITTLE_ENDIAN;
			else return null;
		}
		else {
			throw new IllegalArgumentException("Byte width not supported: " + b.length);
		}
	}

	public static ByteOrder testByteOrder(byte[] b, double testValue) {
		if (b.length == 4) {  // 32-bit case
			return testByteOrder(b, Float.floatToIntBits((float) testValue));
		}
		else if (b.length == 8) {  // 64-bit case
			return testByteOrder(b, Double.doubleToLongBits(testValue));
		}
		else {
			throw new IllegalArgumentException("Byte width not supported: " + b.length);
		}
	}

}
