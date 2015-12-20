package net.sandius.rembulan.core.legacy;

import net.sandius.rembulan.util.Check;

public class ReturnTarget {

	protected final Registers registers;
	protected int idx;

	private int state;

	protected ReturnTarget(Registers registers, int offset) {
		Check.notNull(registers);
		Check.inRange(offset, 0, registers.size() - 1);
		this.registers = registers;
		this.idx = offset;

		state = 0;
	}

	protected void requireAndSetState(int current, int next) {
		if (state != current) {
			throw new IllegalStateException("Illegal state: expected to be in " + current + ", was in " + state);
		}
		state = next;
	}

	public void begin() {
		requireAndSetState(0, 1);
		registers.setTop(idx);
	}

	public void push(Object object) {
		requireAndSetState(1, 1);
		registers.push(object);
	}

	public void end() {
		requireAndSetState(1, 2);
		registers.setTop(idx);
	}
}
