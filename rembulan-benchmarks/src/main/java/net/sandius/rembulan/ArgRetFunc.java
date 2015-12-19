package net.sandius.rembulan;

import net.sandius.rembulan.util.Ptr;

public abstract class ArgRetFunc {

	public abstract void call(Ptr<Object[]> result);

	public abstract void call(Ptr<Object[]> result, Object a);

	public abstract void call(Ptr<Object[]> result, Object a, Object b);

	public abstract void call(Ptr<Object[]> result, Object a, Object b, Object c);

	public abstract void call(Ptr<Object[]> result, Object a, Object b, Object c, Object d);

	public abstract void call(Ptr<Object[]> result, Object a, Object b, Object c, Object d, Object e);

	public abstract void call(Ptr<Object[]> result, Object[] args);

	public static abstract class _0 extends ArgRetFunc {

		@Override
		public void call(Ptr<Object[]> result, Object a) {
			call(result);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b) {
			call(result);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c) {
			call(result);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c, Object d) {
			call(result);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c, Object d, Object e) {
			call(result);
		}

		@Override
		public void call(Ptr<Object[]> result, Object[] args) {
			call(result);
		}
	}

	public static abstract class _1 extends ArgRetFunc {

		@Override
		public void call(Ptr<Object[]> result) {
			call(result, null);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b) {
			call(result, a);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c) {
			call(result, a);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c, Object d) {
			call(result, a);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c, Object d, Object e) {
			call(result, a);
		}

		@Override
		public void call(Ptr<Object[]> result, Object[] args) {
			Object a = args.length >= 1 ? args[0] : null;
			call(result, a);
		}
	}

	public static abstract class _2 extends ArgRetFunc {

		@Override
		public void call(Ptr<Object[]> result) {
			call(result, null, null);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a) {
			call(result, a, null);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c) {
			call(result, a, b);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c, Object d) {
			call(result, a, b);
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c, Object d, Object e) {
			call(result, a, b);
		}

		@Override
		public void call(Ptr<Object[]> result, Object[] args) {
			Object a = args.length >= 1 ? args[0] : null;
			Object b = args.length >= 2 ? args[1] : null;
			call(result, a, b);
		}
	}

	public static abstract class _var extends ArgRetFunc {

		@Override
		public void call(Ptr<Object[]> result) {
			call(result, new Object[] { });
		}

		@Override
		public void call(Ptr<Object[]> result, Object a) {
			call(result, new Object[] { a });
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b) {
			call(result, new Object[] { a, b });
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c) {
			call(result, new Object[] { a, b, c });
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c, Object d) {
			call(result, new Object[] { a, b, c, d });
		}

		@Override
		public void call(Ptr<Object[]> result, Object a, Object b, Object c, Object d, Object e) {
			call(result, new Object[] { a, b, c, d, e });
		}
	}

}
