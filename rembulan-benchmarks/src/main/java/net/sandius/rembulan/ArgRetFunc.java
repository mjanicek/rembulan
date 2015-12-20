package net.sandius.rembulan;

import net.sandius.rembulan.util.ObjectSink;

public abstract class ArgRetFunc {

	public abstract void invoke(ObjectSink result);

	public abstract void invoke(ObjectSink result, Object a);

	public abstract void invoke(ObjectSink result, Object a, Object b);

	public abstract void invoke(ObjectSink result, Object a, Object b, Object c);

	public abstract void invoke(ObjectSink result, Object a, Object b, Object c, Object d);

	public abstract void invoke(ObjectSink result, Object a, Object b, Object c, Object d, Object e);

	public abstract void invoke(ObjectSink result, Object[] args);

	public abstract void resume(ObjectSink result, int pc, Object[] registers);

	public static abstract class _0p extends ArgRetFunc {

		@Override
		public void invoke(ObjectSink result, Object a) {
			invoke(result);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b) {
			invoke(result);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c) {
			invoke(result);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c, Object d) {
			invoke(result);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c, Object d, Object e) {
			invoke(result);
		}

		@Override
		public void invoke(ObjectSink result, Object[] args) {
			invoke(result);
		}
	}

	public static abstract class _1p extends ArgRetFunc {

		@Override
		public void invoke(ObjectSink result) {
			invoke(result, null);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b) {
			invoke(result, a);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c) {
			invoke(result, a);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c, Object d) {
			invoke(result, a);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c, Object d, Object e) {
			invoke(result, a);
		}

		@Override
		public void invoke(ObjectSink result, Object[] args) {
			Object a = args.length >= 1 ? args[0] : null;
			invoke(result, a);
		}
	}

	public static abstract class _2p extends ArgRetFunc {

		@Override
		public void invoke(ObjectSink result) {
			invoke(result, null, null);
		}

		@Override
		public void invoke(ObjectSink result, Object a) {
			invoke(result, a, null);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c) {
			invoke(result, a, b);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c, Object d) {
			invoke(result, a, b);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c, Object d, Object e) {
			invoke(result, a, b);
		}

		@Override
		public void invoke(ObjectSink result, Object[] args) {
			Object a = args.length >= 1 ? args[0] : null;
			Object b = args.length >= 2 ? args[1] : null;
			invoke(result, a, b);
		}

		public static abstract class _2r extends _2p {

			@Override
			public void invoke(ObjectSink result, Object a, Object b) {
				resume(result, 0, a, b);
			}

			@Override
			public void resume(ObjectSink result, int pc, Object[] regs) {
				resume(result, pc, regs[0], regs[1]);
			}

			protected abstract void resume(ObjectSink result, int pc, Object r_0, Object r_1);

		}

		public static abstract class _3r extends _2p {

			@Override
			public void invoke(ObjectSink result, Object a, Object b) {
				resume(result, 0, a, b, null);
			}

			@Override
			public void resume(ObjectSink result, int pc, Object[] regs) {
				resume(result, pc, regs[0], regs[1], regs[2]);
			}

			protected abstract void resume(ObjectSink result, int pc, Object r_0, Object r_1, Object r_2);

		}

	}

	public static abstract class _3p extends ArgRetFunc {

		@Override
		public void invoke(ObjectSink result) {
			invoke(result, null, null, null);
		}

		@Override
		public void invoke(ObjectSink result, Object a) {
			invoke(result, a, null, null);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b) {
			invoke(result, a, b, null);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c, Object d) {
			invoke(result, a, b, c);
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c, Object d, Object e) {
			invoke(result, a, b, c);
		}

		@Override
		public void invoke(ObjectSink result, Object[] args) {
			Object a = args.length >= 1 ? args[0] : null;
			Object b = args.length >= 2 ? args[1] : null;
			Object c = args.length >= 3 ? args[2] : null;
			invoke(result, a, b, c);
		}

		public static abstract class _2r extends _3p {

			@Override
			public void invoke(ObjectSink result, Object a, Object b, Object c) {
				resume(result, 0, a, b);
			}

			@Override
			public void resume(ObjectSink result, int pc, Object[] regs) {
				resume(result, pc, regs[0], regs[1]);
			}

			protected abstract void resume(ObjectSink result, int pc, Object r_0, Object r_1);

		}

		public static abstract class _3r extends _3p {

			@Override
			public void invoke(ObjectSink result, Object a, Object b, Object c) {
				resume(result, 0, a, b, c);
			}

			@Override
			public void resume(ObjectSink result, int pc, Object[] regs) {
				resume(result, pc, regs[0], regs[1], regs[2]);
			}

			protected abstract void resume(ObjectSink result, int pc, Object r_0, Object r_1, Object r_2);

		}

	}

	public static abstract class _vp extends ArgRetFunc {

		@Override
		public void invoke(ObjectSink result) {
			invoke(result, new Object[] { });
		}

		@Override
		public void invoke(ObjectSink result, Object a) {
			invoke(result, new Object[] { a });
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b) {
			invoke(result, new Object[] { a, b });
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c) {
			invoke(result, new Object[] { a, b, c });
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c, Object d) {
			invoke(result, new Object[] { a, b, c, d });
		}

		@Override
		public void invoke(ObjectSink result, Object a, Object b, Object c, Object d, Object e) {
			invoke(result, new Object[] { a, b, c, d, e });
		}
	}

}
