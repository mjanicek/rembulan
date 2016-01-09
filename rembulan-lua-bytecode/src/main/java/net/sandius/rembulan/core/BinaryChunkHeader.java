package net.sandius.rembulan.core;

public abstract class BinaryChunkHeader {

	public static final String SIGNATURE = "\u001bLua";
	public static final String TAIL = "\u0019\u0093\r\n\u001a\n";

	public static final int VERSION = 0x53;
	public static final int FORMAT = 0;

	public static final long BYTE_ORDER_TEST_INTEGER = 0x5678L;
	public static final double BYTE_ORDER_TEST_FLOAT = 370.5;

	private BinaryChunkHeader() {
		// not to be instantiated
	}

}
