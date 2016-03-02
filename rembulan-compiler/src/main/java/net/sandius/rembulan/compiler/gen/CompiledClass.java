package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.ByteVector;
import net.sandius.rembulan.util.Check;

public class CompiledClass {

	public final String name;
	public final ByteVector bytes;

	public CompiledClass(String name, ByteVector bytes) {
		this.name = Check.notNull(name);
		this.bytes = Check.notNull(bytes);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CompiledClass that = (CompiledClass) o;

		return name.equals(that.name) && bytes.equals(that.bytes);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + bytes.hashCode();
		return result;
	}

	public String name() {
		return name;
	}

	public ByteVector bytes() {
		return bytes;
	}

}
