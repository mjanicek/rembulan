package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public abstract class LoadConst extends IRNode {

	public static class Nil extends LoadConst {

		public Nil() {
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

	}
	
	public static class Bool extends LoadConst {
		
		private final boolean value;
		
		public Bool(boolean value) {
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

		public Int(long value) {
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

		public Flt(double value) {
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

		public Str(String value) {
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
