package net.sandius.rembulan.test;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Resumable;
import net.sandius.rembulan.core.ResumeInfo;
import net.sandius.rembulan.core.Upvalue;

public class java_Upvalues3 extends Object {

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
					r_2 = sink._1();
					if (r_2 == null) {
						r_0 = new Upvalue(r_0);
						r_2 = new f1((Upvalue) r_0);
						r_1 = r_2;
						r_1 = new Upvalue(r_1);
					}
					else {
						r_1 = new Upvalue(r_1);
						r_2 = new f2((Upvalue) r_1);
						r_0 = r_2;
						r_0 = new Upvalue(r_0);
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
			ct.push(new ResumeInfo((Resumable) this, new ResumeInfo.SavedState(rp, new Object[] {r_0, r_1, r_2 })));
			throw ct;
		}
	}

	public static class f1 extends Object {

		protected final Upvalue x;

		public f1(Upvalue x) {
			super();
			this.x = x;
		}

		private void run(LuaState state, ObjectSink sink, int rp, Object r_0, Object r_1) throws ControlThrowable {
			r_1 = x.get();
			sink.setTo(r_1);
		}

	}

	public static class f2 extends Object {

		protected final Upvalue y;

		public f2(Upvalue y) {
			super();
			this.y = y;
		}

		private void run(LuaState state, ObjectSink sink, int rp, Object r_0, Object r_1) throws ControlThrowable {
			r_1 = y.get();
			sink.setTo(r_1);
		}

	}


}
