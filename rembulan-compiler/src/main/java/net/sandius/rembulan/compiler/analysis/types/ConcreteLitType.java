package net.sandius.rembulan.compiler.analysis.types;

public class ConcreteLitType<T> extends ConcreteType {

	protected ConcreteLitType(AbstractType supertype, String name) {
		super(supertype, name);
	}

	public LiteralType<T> newLiteralType(T value) {
		return new LiteralType<>(this, value);
	}

}
