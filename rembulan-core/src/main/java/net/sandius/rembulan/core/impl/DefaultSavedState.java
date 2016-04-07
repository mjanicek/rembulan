package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.util.Check;

import java.io.Serializable;

public class DefaultSavedState implements Serializable {

	public final int resumptionPoint;
	public final Object[] registers;
	public final Object[] varargs;

	public DefaultSavedState(int resumptionPoint, Object[] registers, Object[] varargs) {
		this.resumptionPoint = resumptionPoint;
		this.registers = Check.notNull(registers);
		this.varargs = varargs;
	}

	public DefaultSavedState(int resumptionPoint, Object[] registers) {
		this(resumptionPoint, registers, null);
	}

}
