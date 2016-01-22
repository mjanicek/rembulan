package net.sandius.rembulan.compiler.gen;

public abstract class NUnconditional extends NNode {

	protected NNode next;

	public NUnconditional(NNode next) {
		super();
		this.next = next;
	}

	public NUnconditional() {
		this(null);
	}

	@Override
	public String nextToString() {
		return next != null ? next.toString() : "NULL";
	}

	public NNode next() {
		return next;
	}

	public NUnconditional append(NNode n) {
		this.next = n;
		return this;
	}

	public void insertAfter(NUnconditional n) {
		n.next = this.next;
		this.next = n;
	}

}
