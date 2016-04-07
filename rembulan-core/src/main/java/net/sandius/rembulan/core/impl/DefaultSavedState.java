package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.io.Serializable;

public class DefaultSavedState implements Serializable {

	private final int resumptionPoint;
	private final ReadOnlyArray<Object> registers;
	private final ReadOnlyArray<Object> varargs;

	public DefaultSavedState(int resumptionPoint, Object[] registers, Object[] varargs) {
		this.resumptionPoint = resumptionPoint;
		this.registers = ReadOnlyArray.copyFrom(Check.notNull(registers));
		this.varargs = varargs != null ? ReadOnlyArray.copyFrom(varargs) : null;
	}

	public DefaultSavedState(int resumptionPoint, Object[] registers) {
		this(resumptionPoint, registers, null);
	}

	public int resumptionPoint() {
		return resumptionPoint;
	}

	public Object[] registers() {
		return registers.copyToNewArray();
	}

	public Object[] varargs() {
		return varargs != null ? varargs.copyToNewArray() : null;
	}

}
