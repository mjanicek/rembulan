package net.sandius.rembulan.lbc;

public abstract class BinaryChunkConstants {

	public static final String SIGNATURE = "\u001bLua";
	public static final String TAIL = "\u0019\u0093\r\n\u001a\n";

	public static final int VERSION = 0x53;
	public static final int FORMAT = 0;

	public static final long BYTE_ORDER_TEST_INTEGER = 0x5678L;
	public static final double BYTE_ORDER_TEST_FLOAT = 370.5;

	// type tags of constants
	public static final int CONST_NIL = 0;
	public static final int CONST_BOOLEAN = 1;
	public static final int CONST_FLOAT = 3;
	public static final int CONST_SHORT_STRING = 4;
	public static final int CONST_INTEGER = CONST_FLOAT | (1 << 4);
	public static final int CONST_LONG_STRING = CONST_SHORT_STRING | (1 << 4);

	private BinaryChunkConstants() {
		// not to be instantiated
	}

}
