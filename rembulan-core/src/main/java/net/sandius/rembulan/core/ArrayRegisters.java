package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class ArrayRegisters implements Registers {

	private int top;
	private final Object[] regs;

	public ArrayRegisters(int size) {
		Check.nonNegative(size);
		regs = new Object[size];
		top = 0;
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
		top = newTop;
	}

	@Override
	public Registers from(int offset) {
		return new View(this, offset);
	}

	protected static class View implements Registers {

		protected final ArrayRegisters parent;
		protected final int offset;

		protected View(ArrayRegisters parent, int offset) {
			Check.notNull(parent);
			this.parent = parent;
			this.offset = offset;
		}

		@Override
		public Object get(int idx) {
			return parent.get(idx + offset);
		}

		@Override
		public void set(int idx, Object object) {
			parent.set(idx + offset, object);
		}

		@Override
		public int getTop() {
			return parent.getTop() - offset;
		}

		@Override
		public void setTop(int newTop) {
			parent.setTop(newTop + offset);
		}

		@Override
		public Registers from(int offset) {
			return parent.from(this.offset + offset);
		}
	}

}
