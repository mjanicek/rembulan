package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.GenericBuilder;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.ArrayList;

public class Prototype {

	/* constants used by the function */
	private final ReadOnlyArray<Object> consts;

	private final IntVector code;

	/* functions defined inside the function */
	private final ReadOnlyArray<Prototype> p;

	/* map from opcodes to source lines */
	private final IntVector lineinfo;

	/* information about local variables */
	private final ReadOnlyArray<LocalVariable> locvars;

	/* upvalue information */
	private final ReadOnlyArray<Upvalue.Desc> upvalues;

	private final String source;

	private final int linedefined;

	private final int lastlinedefined;

	private final int numparams;

	private final boolean is_vararg;

	private final int maxstacksize;

	public Prototype(
			ReadOnlyArray<Object> consts,
			IntVector code,
			ReadOnlyArray<Prototype> p,
			IntVector lineinfo,
			ReadOnlyArray<LocalVariable> locvars,
			ReadOnlyArray<Upvalue.Desc> upvalues,
			String source,
			int linedefined,
			int lastlinedefined,
			int numparams,
			boolean is_vararg,
			int maxstacksize) {

		Check.notNull(consts);
		Check.notNull(code);
		Check.notNull(p);
		// lineinfo may be null
		Check.notNull(locvars);
		Check.notNull(upvalues);
		Check.notNull(source);

		this.consts = consts;
		this.code = code;
		this.p = p;
		this.lineinfo = lineinfo;
		this.locvars = locvars;
		this.upvalues = upvalues;
		this.source = source;
		this.linedefined = linedefined;
		this.lastlinedefined = lastlinedefined;
		this.numparams = numparams;
		this.is_vararg = is_vararg;
		this.maxstacksize = maxstacksize;
	}

	public static Prototype newEmptyPrototype(int n_upvalues) {
		return new Prototype(
				new ReadOnlyArray<Object>(new Object[0]),
				IntVector.wrap(new int[0]),
				new ReadOnlyArray<Prototype>(new Prototype[0]),
				IntVector.wrap(new int[0]),
				new ReadOnlyArray<LocalVariable>(new LocalVariable[0]),
				new ReadOnlyArray<Upvalue.Desc>(new Upvalue.Desc[n_upvalues]),
				null,
				0, 0, 0, false, 0);
	}

	public static Prototype newEmptyPrototype() {
		return Prototype.newEmptyPrototype(0);
	}

	public ReadOnlyArray<Object> getConstants() {
		return consts;
	}

	public IntVector getCode() {
		return code;
	}

	public ReadOnlyArray<Prototype> getNestedPrototypes() {
		return p;
	}

	/** Get the name of a local variable.
	 *
	 * @param number the local variable number to look up
	 * @param pc the program counter
	 * @return the name, or null if not found
	 */
	public String getLocalVariableName(int number, int pc) {
		for (int i = 0; i < locvars.size() && locvars.get(i).beginPC <= pc; i++) {
			if (pc < locvars.get(i).endPC) {  // is variable active?
				number--;
				if (number == 0) {
					return locvars.get(i).variableName;
				}
			}
		}
		return null;  // not found
	}

	public ReadOnlyArray<LocalVariable> getLocalVariables() {
		return locvars;
	}

	public ReadOnlyArray<Upvalue.Desc> getUpValueDescriptions() {
		return upvalues;
	}

	public boolean hasUpValues() {
		return !upvalues.isEmpty();
	}

	public boolean hasLineInfo() {
		return lineinfo != null;
	}

	public int getLineAtPC(int pc) {
		return hasLineInfo() ? pc >= 0 && pc < lineinfo.length() ? lineinfo.get(pc) : -1 : -1;
	}

	public int getBeginLine() {
		return linedefined;
	}

	public int getEndLine() {
		return lastlinedefined;
	}

	public int getNumberOfParameters() {
		return numparams;
	}

	public boolean isVararg() {
		return is_vararg;
	}

	public int getMaximumStackSize() {
		return maxstacksize;
	}

	public String toString() {
		return source + ":" + linedefined + "-" + lastlinedefined;
	}

	public String getSource() {
		return source;
	}

	public String getShortSource() {
//		String name = source.tojstring();
		String name = source;
        if (name.startsWith("@") || name.startsWith("=")) {
			name = name.substring(1);
		}
		else if (name.startsWith("\033")) {
			name = "binary string";
		}
        return name;
	}

	public static class Builder implements GenericBuilder<Prototype> {
		public final ArrayList<Object> constants;
		public final IntVector.Builder code;
		public final ArrayList<Builder> p;

		public final IntVector.Builder lineinfo;

		public final ArrayList<LocalVariable.Builder> locvars;
		public final ArrayList<Upvalue.Desc.Builder> upvalues;

		public String source;
		public int linedefined;
		public int lastlinedefined;
		public int numparams;
		public boolean is_vararg;
		public int maxstacksize;

		public Builder() {
			this.constants = new ArrayList<Object>();
			this.code = IntVector.newBuilder();
			this.p = new ArrayList<Prototype.Builder>();
			this.lineinfo = IntVector.newBuilder();;
			this.locvars = new ArrayList<LocalVariable.Builder>();
			this.upvalues = new ArrayList<Upvalue.Desc.Builder>();
		}

		// FIXME: ugly! is there a point in having this even?
		public Builder(Prototype proto) {
			this.constants = new ArrayList<Object>();
			for (Object c : proto.consts) {
				constants.add(c);
			}

			this.p = new ArrayList<Prototype.Builder>();
			for (Prototype pp : proto.p) {
				this.p.add(new Builder(pp));
			}

			this.lineinfo = IntVector.newBuilder().set(proto.lineinfo.copyToNewArray());

			this.locvars = new ArrayList<LocalVariable.Builder>();
			for (LocalVariable lv : proto.locvars) {
				this.locvars.add(new LocalVariable.Builder(lv.variableName, lv.beginPC, lv.endPC));
			}

			this.upvalues = new ArrayList<Upvalue.Desc.Builder>();
			for (Upvalue.Desc uv : proto.upvalues) {
				this.upvalues.add(new Upvalue.Desc.Builder(uv.name, uv.inStack, uv.index));
			}

			this.code = IntVector.newBuilder();
			this.code.set(proto.code);

			this.source = proto.source;
			this.linedefined = proto.linedefined;
			this.lastlinedefined = proto.lastlinedefined;
			this.numparams = proto.numparams;
			this.is_vararg = proto.is_vararg;
			this.maxstacksize = proto.maxstacksize;
		}

		@Override
		public Prototype build() {
			Object[] cs0 = new Object[this.constants.size()];
			for (int i = 0; i < this.constants.size(); i++) {
				cs0[i] = this.constants.get(i);
			}
			ReadOnlyArray<Object> consts = new ReadOnlyArray<Object>(cs0);

			Prototype[] ps0 = new Prototype[this.p.size()];
			for (int i = 0; i < this.p.size(); i++) {
				ps0[i] = this.p.get(i).build();
			}
			ReadOnlyArray<Prototype> ps = new ReadOnlyArray<Prototype>(ps0);

			LocalVariable[] lvs0 = new LocalVariable[this.locvars.size()];
			for (int i = 0; i < this.locvars.size(); i++) {
				lvs0[i] = this.locvars.get(i).build();
			}
			ReadOnlyArray<LocalVariable> lvs = new ReadOnlyArray<LocalVariable>(lvs0);

			Upvalue.Desc[] uvs0 = new Upvalue.Desc[this.upvalues.size()];
			for (int i = 0; i < this.upvalues.size(); i++) {
				uvs0[i] = this.upvalues.get(i).build();
			}
			ReadOnlyArray<Upvalue.Desc> uvs = new ReadOnlyArray<Upvalue.Desc>(uvs0);

			return new Prototype(
					consts,
					code.build(),
					ps,
					lineinfo.build(),
					lvs,
					uvs,
					source,
					linedefined,
					lastlinedefined,
					numparams,
					is_vararg,
					maxstacksize);
		}

	}

}
