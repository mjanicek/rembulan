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

	public NLabel() {
		this(null);
	}

	@Override
	public String selfToString() {
		return "@" + (name != null ? name : Integer.toHexString(System.identityHashCode(this)));
	}

	public static NLabel guard(NNode n) {
		if (n instanceof NLabel) {
			return (NLabel) n;
		}
		else {
			// insert a label node in front of n
			NLabel l = new NLabel();
			l.insertBefore(n);
			return l;
		}
	}

}
