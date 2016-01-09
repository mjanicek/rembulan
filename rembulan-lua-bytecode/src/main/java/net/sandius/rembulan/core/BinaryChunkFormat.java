package net.sandius.rembulan.core;

import java.nio.ByteOrder;
import java.util.Objects;

public final class BinaryChunkFormat {

	public final ByteOrder byteOrder;

	public final int sizeOfInt;
	public final int sizeOfSizeT;
	public final int sizeOfInstruction;
	public final int sizeOfLuaInteger;
	public final int sizeOfLuaFloat;

	public BinaryChunkFormat(ByteOrder byteOrder, int sizeOfInt, int sizeOfSizeT, int sizeOfInstruction, int sizeOfLuaInteger, int sizeOfLuaFloat) {
		this.byteOrder = Objects.requireNonNull(byteOrder);
		this.sizeOfInt = sizeOfInt;
		this.sizeOfSizeT = sizeOfSizeT;
		this.sizeOfInstruction = sizeOfInstruction;
		this.sizeOfLuaInteger = sizeOfLuaInteger;
		this.sizeOfLuaFloat = sizeOfLuaFloat;
	}

	@Override
	public String toString() {
		return "["
				+ (byteOrder == ByteOrder.BIG_ENDIAN ? "BE" : "LE") + " "
				+ "int:" + sizeOfInt + " "
				+ "size_t:" + sizeOfSizeT + " "
				+ "instr:" + sizeOfInstruction + " "
				+ "luaInt:" + sizeOfLuaInteger + " "
				+ "luaFlt:" + sizeOfLuaFloat
				+ "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BinaryChunkFormat that = (BinaryChunkFormat) o;

		if (sizeOfInt != that.sizeOfInt) return false;
		if (sizeOfSizeT != that.sizeOfSizeT) return false;
		if (sizeOfInstruction != that.sizeOfInstruction) return false;
		if (sizeOfLuaInteger != that.sizeOfLuaInteger) return false;
		if (sizeOfLuaFloat != that.sizeOfLuaFloat) return false;
		return byteOrder.equals(that.byteOrder);
	}

	@Override
	public int hashCode() {
		int result = byteOrder.hashCode();
		result = 31 * result + sizeOfInt;
		result = 31 * result + sizeOfSizeT;
		result = 31 * result + sizeOfInstruction;
		result = 31 * result + sizeOfLuaInteger;
		result = 31 * result + sizeOfLuaFloat;
		return result;
	}

}
