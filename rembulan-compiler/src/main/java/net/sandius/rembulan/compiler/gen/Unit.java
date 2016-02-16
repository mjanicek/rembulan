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

	public void setGeneric(TypeSeq returnType) {
		this.generic = new CompiledPrototype(genericParameters(), returnType);
	}

	public class CompiledPrototype {

		private final TypeSeq actualParameters;
		private final TypeSeq returnType;

		protected CompiledPrototype(TypeSeq actualParameters, TypeSeq returnType) {
			this.actualParameters = Objects.requireNonNull(actualParameters);
			this.returnType = Objects.requireNonNull(returnType);
		}

		public TypeSeq actualParameters() {
			return actualParameters;
		}

		public TypeSeq returnType() {
			return returnType;
		}

		public Type.FunctionType functionType() {
			return Type.FunctionType.of(actualParameters(), returnType());
		}

	}

}
