package net.sandius.rembulan.core;

public class PlainValueTypeNamer implements ValueTypeNamer {

	public static final PlainValueTypeNamer INSTANCE = new PlainValueTypeNamer();

	@Override
	public String typeNameOf(Object instance) {
		return Value.typeOf(instance).name;
	}

}
