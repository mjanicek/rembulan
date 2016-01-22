package net.sandius.rembulan.compiler.gen;

public class NLabel extends NUnconditional {

	public final String name;

	public NLabel(String name, NNode n) {
		super(n);
		this.name = name;
	}

	public NLabel(String name) {
		this(name, null);
	}

	@Override
	public String selfToString() {
		return "@" + (name != null ? name : Integer.toHexString(System.identityHashCode(this)));
	}

}
