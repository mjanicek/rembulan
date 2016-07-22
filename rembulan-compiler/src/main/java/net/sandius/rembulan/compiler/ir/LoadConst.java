package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public abstract class LoadConst extends BodyNode {

	private final Val dest;

	private LoadConst(Val dest) {
		this.dest = Check.notNull(dest);
	}

	public Val dest() {
		return dest;
	}

	public static class Nil extends LoadConst {

		public Nil(Val dest) {
			super(dest);
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

	}
	
	public static class Bool extends LoadConst {
		
		private final boolean value;
		
		public Bool(Val dest, boolean value) {
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

		public Int(Val dest, long value) {
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

		public Flt(Val dest, double value) {
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

		public Str(Val dest, String value) {
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
