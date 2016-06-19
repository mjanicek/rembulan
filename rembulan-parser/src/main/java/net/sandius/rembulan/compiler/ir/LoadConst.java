package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public abstract class LoadConst extends IRNode {

	private final Temp dest;

	private LoadConst(Temp dest) {
		this.dest = Check.notNull(dest);
	}

	public Temp dest() {
		return dest;
	}

	public static class Nil extends LoadConst {

		public Nil(Temp dest) {
			super(dest);
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

	}
	
	public static class Bool extends LoadConst {
		
		private final boolean value;
		
		public Bool(Temp dest, boolean value) {
			super(dest);
			this.value = value;
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

		public boolean value() {
			return value;
		}

	}

	public static class Int extends LoadConst {

		private final long value;

		public Int(Temp dest, long value) {
			super(dest);
			this.value = value;
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

		public long value() {
			return value;
		}

	}

	public static class Flt extends LoadConst {

		private final double value;

		public Flt(Temp dest, double value) {
			super(dest);
			this.value = value;
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

		public double value() {
			return value;
		}

	}

	public static class Str extends LoadConst {

		private final String value;

		public Str(Temp dest, String value) {
			super(dest);
			this.value = Check.notNull(value);
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

		public String value() {
			return value;
		}

	}

}
