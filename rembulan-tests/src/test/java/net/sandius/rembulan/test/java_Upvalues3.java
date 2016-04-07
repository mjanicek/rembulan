package net.sandius.rembulan.test;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.ResumeInfo;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.core.impl.Function0;

import java.io.Serializable;

public class java_Upvalues3 extends Function0 {

	protected final Upvalue _ENV;

	public java_Upvalues3(Upvalue _ENV) {
		super();
		this._ENV = _ENV;
	}

	private void run(LuaState state, ObjectSink sink, int rp, Object r_0, Object r_1, Object r_2) throws ControlThrowable {
		try {
			switch (rp) {
				case 0:
					r_0 = null;
					r_1 = null;
					rp = 1;
					Dispatch.index(state, sink, _ENV.get(), "g");
				case 1:
					r_2 = sink._0();
					if (r_2 == null) {
						r_0 = state.newUpvalue(r_0);
						r_2 = new f1((Upvalue) r_0);
						r_1 = r_2;
						r_1 = state.newUpvalue(r_1);
					}
					else {
						r_1 = state.newUpvalue(r_1);
						r_2 = new f2((Upvalue) r_1);
						r_0 = r_2;
						r_0 = state.newUpvalue(r_0);
					}
					if (((Upvalue) r_0).get() != null) {
						r_2 = ((Upvalue) r_1).get();
					}
					else {
						r_2 = ((Upvalue) r_0).get();
					}
					sink.setTo(r_2);
					return;

				default:
					throw new IllegalStateException();
			}
		}
		catch (ControlThrowable ct) {
			ct.push(this, snapshot(rp, r_0, r_1, r_2));
			throw ct;
		}
	}

	private Serializable snapshot(int rp, Object r_0, Object r_1, Object r_2) {
		return new ResumeInfo.SavedState(rp, new Object[] { r_0, r_1, r_2 });
	}

	@Override
	public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
		ResumeInfo.SavedState ss = (ResumeInfo.SavedState) suspendedState;
		Object[] regs = ss.registers;
		run(state, result, ss.resumptionPoint, regs[0], regs[1], regs[2]);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result) throws ControlThrowable {
		run(state, result, 0, null, null, null);
	}

	public static class f1 extends Function0 {

		protected final Upvalue x;

		public f1(Upvalue x) {
			super();
			this.x = x;
		}

		private void run(LuaState state, ObjectSink sink, int rp, Object r_0, Object r_1) throws ControlThrowable {
			r_0 = x.get();
			sink.setTo(r_0);
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
			throw new UnsupportedOperationException();
		}

		@Override
		public void invoke(LuaState state, ObjectSink result) throws ControlThrowable {
			run(state, result, 0, null, null);
		}

	}

	public static class f2 extends Function0 {

		protected final Upvalue y;

		public f2(Upvalue y) {
			super();
			this.y = y;
		}

		private void run(LuaState state, ObjectSink sink, int rp, Object r_0, Object r_1) throws ControlThrowable {
			r_0 = y.get();
			sink.setTo(r_0);
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
			throw new UnsupportedOperationException();
		}

		@Override
		public void invoke(LuaState state, ObjectSink result) throws ControlThrowable {
			run(state, result, 0, null, null);
		}

	}


}
