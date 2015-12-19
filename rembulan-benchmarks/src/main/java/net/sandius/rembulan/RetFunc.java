package net.sandius.rembulan;

public abstract class RetFunc {

	public abstract Object[] call();

	public abstract Object[] call(Object a);

	public abstract Object[] call(Object a, Object b);

	public abstract Object[] call(Object a, Object b, Object c);

	public abstract Object[] call(Object a, Object b, Object c, Object d);

	public abstract Object[] call(Object a, Object b, Object c, Object d, Object e);

	public abstract Object[] call(Object[] args);

	public static abstract class _0 extends RetFunc {

		@Override
		public Object[] call(Object a) {
			return call();
		}

		@Override
		public Object[] call(Object a, Object b) {
			return call();
		}

		@Override
		public Object[] call(Object a, Object b, Object c) {
			return call();
		}

		@Override
		public Object[] call(Object a, Object b, Object c, Object d) {
			return call();
		}

		@Override
		public Object[] call(Object a, Object b, Object c, Object d, Object e) {
			return call();
		}

		@Override
		public Object[] call(Object[] args) {
			return call();
		}
	}

	public static abstract class _1 extends RetFunc {

		@Override
		public Object[] call() {
			return call(null);
		}

		@Override
		public Object[] call(Object a, Object b) {
			return call(a);
		}

		@Override
		public Object[] call(Object a, Object b, Object c) {
			return call(a);
		}

		@Override
		public Object[] call(Object a, Object b, Object c, Object d) {
			return call(a);
		}

		@Override
		public Object[] call(Object a, Object b, Object c, Object d, Object e) {
			return call(a);
		}

		@Override
		public Object[] call(Object[] args) {
			Object a = args.length >= 1 ? args[0] : null;
			return call(a);
		}
	}

	public static abstract class _2 extends RetFunc {

		@Override
		public Object[] call() {
			return call(null, null);
		}

		@Override
		public Object[] call(Object a) {
			return call(a, null);
		}

		@Override
		public Object[] call(Object a, Object b, Object c) {
			return call(a, b);
		}

		@Override
		public Object[] call(Object a, Object b, Object c, Object d) {
			return call(a, b);
		}

		@Override
		public Object[] call(Object a, Object b, Object c, Object d, Object e) {
			return call(a, b);
		}

		@Override
		public Object[] call(Object[] args) {
			Object a = args.length >= 1 ? args[0] : null;
			Object b = args.length >= 2 ? args[1] : null;
			return call(a, b);
		}
	}

	public static abstract class _var extends RetFunc {

		@Override
		public Object[] call() {
			return call(new Object[] { });
		}

		@Override
		public Object[] call(Object a) {
			return call(new Object[] { a });
		}

		@Override
		public Object[] call(Object a, Object b) {
			return call(new Object[] { a, b });
		}

		@Override
		public Object[] call(Object a, Object b, Object c) {
			return call(new Object[] { a, b, c });
		}

		@Override
		public Object[] call(Object a, Object b, Object c, Object d) {
			return call(new Object[] { a, b, c, d });
		}

		@Override
		public Object[] call(Object a, Object b, Object c, Object d, Object e) {
			return call(new Object[] { a, b, c, d, e });
		}

	}

}
