package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.io.DataOutputStream;
import java.io.IOException;

public class PrototypeDumper {

	public final boolean littleEndian;
	public final DataOutputStream out;
	public final boolean strip;

	public PrototypeDumper(boolean littleEndian, DataOutputStream out, boolean strip) {
		Check.notNull(out);
		this.littleEndian = littleEndian;
		this.out = out;
		this.strip = strip;
	}

	public void dumpBoolean(boolean b) throws IOException {
		out.writeByte(b ? 1 : 0);
	}

	public void dumpInt32(int i) throws IOException {
		if (littleEndian) {
			out.writeByte(i & 0xff);
			out.writeByte((i >> 8) & 0xff);
			out.writeByte((i >> 16) & 0xff);
			out.writeByte((i >> 24) & 0xff);
		}
		else {
			out.writeInt(i);
		}
	}

	public void dumpInt64(long l) throws IOException {
		if (littleEndian) {
			dumpInt32((int) l);
			dumpInt32((int) (l >> 32));
		}
		else {
			out.writeLong(l);
		}
	}

	public void dumpString(String s) throws IOException {
		Check.notNull(s);

		int size = s.length() + 1;
		if (size < 0xff) {
			out.writeByte(size);
		}
		else {
			out.writeByte(0xff);
			dumpInt32(size);
		}

		byte[] bytes = s.getBytes();
		out.write(bytes, 0, bytes.length);
		// no need to write the trailing '\0'
	}

	public void dumpDouble(double d) throws IOException {
		dumpInt64(Double.doubleToLongBits(d));
	}

	public void dumpConstNil() throws IOException {
		out.writeByte(PrototypeLoader.LUA_TNIL);
	}

	public void dumpConstBoolean(boolean value) throws IOException {
		out.writeByte(PrototypeLoader.LUA_TBOOLEAN);
		dumpBoolean(value);
	}

	public void dumpConstInteger(long value) throws IOException {
		out.writeByte(PrototypeLoader.LUA_TNUMINT);
		dumpInt64(value);
	}

	public void dumpConstFloat(double value) throws IOException {
		out.writeByte(PrototypeLoader.LUA_TNUMFLT);
		dumpDouble(value);
	}

	public void dumpConstString(String value) throws IOException {
		Check.notNull(value);
		out.writeByte(PrototypeLoader.LUA_TSTRING);
		dumpString(value);
	}

	public void dumpIntVector(IntVector iv) throws IOException {
		Check.notNull(iv);

		dumpInt32(iv.length());
		for (int i = 0; i < iv.length(); i++) {
			dumpInt32(iv.get(i));
		}
	}

	public void dumpConstants(Constants constants) throws IOException {
		Check.notNull(constants);

		dumpInt32(constants.size());
		for (int i = 0; i < constants.size(); i++) {
			if (constants.isNil(i)) dumpConstNil();
			else if (constants.isBoolean(i)) dumpConstBoolean(constants.getBoolean(i));
			else if (constants.isInteger(i)) dumpConstInteger(constants.getInteger(i));
			else if (constants.isFloat(i)) dumpConstFloat(constants.getFloat(i));
			else if (constants.isString(i)) dumpConstString(constants.getString(i));
			else throw new IllegalArgumentException("Unknown constant #" + i);
		}
	}

	public void dumpNestedPrototypes(ReadOnlyArray<Prototype> protos) throws IOException {
		Check.notNull(protos);

		dumpInt32(protos.size());
		for (int i = 0; i < protos.size(); i++) {
			dumpFunction(protos.get(i), null, true);
		}
	}

	public void dumpUpvalues(ReadOnlyArray<Upvalue.Desc> uvdesc) throws IOException {
		Check.notNull(uvdesc);

		dumpInt32(uvdesc.size());
		for (int i = 0; i < uvdesc.size(); i++) {
			Upvalue.Desc uvd = uvdesc.get(i);
			dumpBoolean(uvd.inStack);
			out.writeByte(uvd.index);
		}
	}

	public void dumpDebug(Prototype proto) throws IOException {
		// TODO: handle the case in which they are not present in the prototype

		// line info
		if (strip) dumpInt32(0);
		else {
			int codeLen = proto.getCode().length();
			dumpInt32(codeLen);
			for (int pc = 0; pc < codeLen; pc++) {
				dumpInt32(proto.getLineAtPC(pc));
			}
		}

		// local var info
		if (strip) dumpInt32(0);
		else {
			ReadOnlyArray<LocalVariable> lvars = proto.getLocalVariables();
			dumpInt32(lvars.size());
			for (int i = 0; i < lvars.size(); i++) {
				LocalVariable lv = lvars.get(i);
				dumpString(lv.variableName);
				dumpInt32(lv.beginPC);
				dumpInt32(lv.endPC);
			}
		}

		// upvalue names
		if (strip) dumpInt32(0);
		else {
			ReadOnlyArray<Upvalue.Desc> uvdesc = proto.getUpValueDescriptions();

			dumpInt32(uvdesc.size());
			for (int i = 0; i < uvdesc.size(); i++) {
				Upvalue.Desc uvd = uvdesc.get(i);
				dumpString(uvd.name);
			}
		}

	}

	private void dumpFunction(Prototype proto, String sourceName, boolean nested) throws IOException {
		Check.notNull(proto);

		// source name
		if (nested) {
			out.writeByte(0);
		}
		else {
			if (strip) dumpInt32(0); else dumpString(proto.getSource());
		}

		dumpInt32(proto.getBeginLine());
		dumpInt32(proto.getEndLine());
		out.writeByte(proto.getNumberOfParameters());
		dumpBoolean(proto.isVararg());
		out.writeByte(proto.getMaximumStackSize());
		dumpIntVector(proto.getCode());
		dumpConstants(proto.getConstants());
		dumpNestedPrototypes(proto.getNestedPrototypes());
		dumpUpvalues(proto.getUpValueDescriptions());
		dumpDebug(proto);
	}

}
