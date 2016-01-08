package net.sandius.rembulan.core;

import net.sandius.rembulan.core.util.ByteArrayDataOutputStream;
import net.sandius.rembulan.util.Check;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class PrototypeWriter extends PrototypeVisitor {

	public final ByteOrder byteOrder;
	public final OutputStream out;

	private final ByteArrayDataOutputStream header;

	private int numCode;
	private final ByteArrayDataOutputStream code;
	private int numConstants;
	private final ByteArrayDataOutputStream constants;
	private int numUpvalues;
	private final ByteArrayDataOutputStream upvalues;

	private final ArrayList<ByteArrayOutputStream> nested;

	private int numLines;
	private final ByteArrayDataOutputStream lines;

	private int numLocals;
	private final ByteArrayDataOutputStream locals;

	private int numUpvalueNames;
	private final ByteArrayDataOutputStream upvalueNames;

	public PrototypeWriter(OutputStream out, ByteOrder byteOrder) {
		Check.notNull(out);
		this.byteOrder = byteOrder;
		this.out = out;

		this.header = ByteArrayDataOutputStream.newInstance(byteOrder);

		this.numCode = 0;
		this.code = ByteArrayDataOutputStream.newInstance(byteOrder);

		this.numConstants = 0;
		this.constants = ByteArrayDataOutputStream.newInstance(byteOrder);

		this.numUpvalues = 0;
		this.upvalues = ByteArrayDataOutputStream.newInstance(byteOrder);

		this.nested = new ArrayList<>();

		this.numLines = 0;
		this.lines = ByteArrayDataOutputStream.newInstance(byteOrder);

		this.numLocals = 0;
		this.locals = ByteArrayDataOutputStream.newInstance(byteOrder);

		this.numUpvalueNames = 0;
		this.upvalueNames = ByteArrayDataOutputStream.newInstance(byteOrder);
	}

	protected static void writeInt(OutputStream out, ByteOrder byteOrder, int v) throws IOException {
		if (byteOrder == ByteOrder.BIG_ENDIAN) {
			out.write((v >>> 24) & 0xFF);
			out.write((v >>> 16) & 0xFF);
			out.write((v >>>  8) & 0xFF);
			out.write((v >>>  0) & 0xFF);
		}
		else if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
			out.write((v >>>  0) & 0xFF);
			out.write((v >>>  8) & 0xFF);
			out.write((v >>> 16) & 0xFF);
			out.write((v >>> 24) & 0xFF);
		}
		else throw new IllegalArgumentException("Illegal byte order");
	}

	protected static void writeString(ByteArrayDataOutputStream bados, String s) throws IOException {
		Check.notNull(s);

		int size = s.length() + 1;
		if (size < 0xff) {
			bados.writeByte(size);
		}
		else {
			bados.writeByte(0xff);
			bados.writeInt(size);
		}

		byte[] bytes = s.getBytes();
		bados.write(bytes, 0, bytes.length);
		// no need to write the trailing '\0'
	}

	@Override
	public void visit(int numParams, boolean vararg, int maxStackSize, String source, int firstLineDefined, int lastLineDefined) {
		try {
			writeString(header, source);
			header.writeInt(firstLineDefined);
			header.writeInt(lastLineDefined);
			header.writeByte(numParams);
			header.writeBoolean(vararg);
			header.writeByte(maxStackSize);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitInstruction(int insn) {
		try {
			code.writeInt(insn);
			numCode += 1;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitNilConst() {
		try {
			constants.writeByte(PrototypeLoader.LUA_TNIL);
			numConstants += 1;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitBooleanConst(boolean value) {
		try {
			constants.writeByte(PrototypeLoader.LUA_TBOOLEAN);
			constants.writeBoolean(value);
			numConstants += 1;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitIntegerConst(long value) {
		try {
			constants.writeByte(PrototypeLoader.LUA_TNUMINT);
			constants.writeLong(value);
			numConstants += 1;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitFloatConst(double value) {
		try {
			constants.writeByte(PrototypeLoader.LUA_TNUMFLT);
			constants.writeDouble(value);
			numConstants += 1;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitStringConst(String value) {
		Check.notNull(value);
		try {
			constants.writeByte(PrototypeLoader.LUA_TSTRING);
			writeString(constants, value);
			numConstants += 1;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitUpvalue(boolean inStack, int index) {
		try {
			upvalues.writeBoolean(inStack);
			upvalues.writeByte(index);
			numUpvalues += 1;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PrototypeVisitor visitNestedPrototype() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrototypeWriter pw = new PrototypeWriter(baos, byteOrder);
		nested.add(baos);
		return pw;
	}

	@Override
	public void visitLine(int line) {
		try {
			lines.writeInt(line);
			numLines += 1;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitUpvalueName(String name) {
		try {
			writeString(upvalueNames, name);
			numUpvalueNames += 1;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitLocalVariable(String name, int beginPC, int endPC) {
		try {
			writeString(locals, name);
			locals.writeInt(beginPC);
			locals.writeInt(endPC);
			numLocals += 1;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visitEnd() {
		try {
			header.writeTo(out);

			writeInt(out, byteOrder, numCode);
			code.writeTo(out);

			writeInt(out, byteOrder, numConstants);
			constants.writeTo(out);

			writeInt(out, byteOrder, numUpvalues);
			upvalues.writeTo(out);

			writeInt(out, byteOrder, nested.size());
			for (ByteArrayOutputStream nestedBaos : nested) {
				nestedBaos.writeTo(out);
			}

			writeInt(out, byteOrder, numLines);
			lines.writeTo(out);

			writeInt(out, byteOrder, numLocals);
			locals.writeTo(out);

			writeInt(out, byteOrder, numUpvalueNames);
			upvalueNames.writeTo(out);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
