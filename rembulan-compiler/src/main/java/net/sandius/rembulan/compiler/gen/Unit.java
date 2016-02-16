package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.Objects;

public class Unit {

	public final Prototype prototype;

	private CompiledPrototype generic;

	public Unit(Prototype prototype) {
		this.prototype = Objects.requireNonNull(prototype);

		this.generic = null;
	}

	public CompiledPrototype generic() {
		return generic;
	}

	public TypeSeq genericParameters() {
		Type[] types = new Type[prototype.getNumberOfParameters()];
		for (int i = 0; i < types.length; i++) {
			types[i] = Type.ANY;
		}
		return new TypeSeq(ReadOnlyArray.wrap(types), prototype.isVararg());
	}

	public void setGeneric(CompiledPrototype cp) {
		this.generic = cp;
	}

}
