package net.sandius.rembulan.core.legacy;

import net.sandius.rembulan.util.Check;

public class FixedSizeRegisters implements Registers {

	private final Object[] regs;
	private int top;

	public FixedSizeRegisters(Object[] array, int top) {
		Check.notNull(array);
		Check.inRange(top, 0, array.length - 1);
		this.regs = array;
		this.top = top;
	}

	public FixedSizeRegisters(int size) {
		this(new Object[size], 0);
	}

	@Override
	public int size() {
		return regs.length;
	}

	@Override
	public void push(Object object) {
		if (top < regs.length) {
			regs[top++] = object;
		}
	}

	@Override
	public Object get(int idx) {
		return regs[idx];
	}

	@Override
	public void set(int idx, Object object) {
		regs[idx] = object;
	}

	@Override
	public int getTop() {
		return top;
	}

	@Override
	public void setTop(int newTop) {
		Check.inRange(newTop, 0, regs.length);
		top = newTop;
	}

	@Override
	public ReturnTarget returnTargetFrom(int from) {
		return new ReturnTarget(this, from);
	}

}
