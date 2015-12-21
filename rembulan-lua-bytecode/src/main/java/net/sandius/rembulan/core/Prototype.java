package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.GenericBuilder;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.ArrayList;

public class Prototype {

	// TODO: split into required and optional debug part

	private final Constants consts;

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
			Constants consts,
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
		// source may be null

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Prototype prototype = (Prototype) o;

		if (linedefined != prototype.linedefined) return false;
		if (lastlinedefined != prototype.lastlinedefined) return false;
		if (numparams != prototype.numparams) return false;
		if (is_vararg != prototype.is_vararg) return false;
		if (maxstacksize != prototype.maxstacksize) return false;
		if (!consts.equals(prototype.consts)) return false;
		if (!code.equals(prototype.code)) return false;
		if (!p.equals(prototype.p)) return false;
		if (lineinfo != null ? !lineinfo.equals(prototype.lineinfo) : prototype.lineinfo != null)
			return false;
		if (!locvars.equals(prototype.locvars)) return false;
		if (!upvalues.equals(prototype.upvalues)) return false;
		return !(source != null ? !source.equals(prototype.source) : prototype.source != null);
	}

	@Override
	public int hashCode() {
		int result = consts.hashCode();
		result = 31 * result + code.hashCode();
		result = 31 * result + p.hashCode();
		result = 31 * result + (lineinfo != null ? lineinfo.hashCode() : 0);
		result = 31 * result + locvars.hashCode();
		result = 31 * result + upvalues.hashCode();
		result = 31 * result + (source != null ? source.hashCode() : 0);
		result = 31 * result + linedefined;
		result = 31 * result + lastlinedefined;
		result = 31 * result + numparams;
		result = 31 * result + (is_vararg ? 1 : 0);
		result = 31 * result + maxstacksize;
		return result;
	}

	public Constants getConstants() {
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
		if (name == null) {
			return "?";
		}
        else if (name.startsWith("@") || name.startsWith("=")) {
			name = name.substring(1);
		}
		else if (name.startsWith("\033")) {
			name = "binary string";
		}
        return name;
	}

}
