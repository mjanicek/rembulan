package net.sandius.rembulan.bm;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.Invokable;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.impl.Function2;
import net.sandius.rembulan.core.impl.Function3;
import net.sandius.rembulan.core.impl.QuintupleCachingObjectSink;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import static net.sandius.rembulan.util.Assertions.assertEquals;

@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 20)
public class CallReturnBenchmark {

	public static ObjectSink newSink() {
		return new QuintupleCachingObjectSink();
	}

	public long primitiveMethod(long n, long l) {
		if (l > 0) {
			return primitiveMethod(n, l - 1) + 1;
		}
		else {
			return n;
		}
	}

	public Object objectMethod(Object n, Object l) {
		long ll = ((Number) l).longValue();
		if (ll > 0) {
			return ((Number) objectMethod(n, ll - 1)).longValue() + 1;
		}
		else {
			return n;
		}
	}

	public Number numberObjectMethod(Number n, Number l) {
		long ll = l.longValue();
		if (ll > 0) {
			return numberObjectMethod(n, ll - 1).longValue() + 1;
		}
		else {
			return n;
		}
	}

	public Long longObjectMethod(Long n, Long l) {
		long ll = l;
		if (ll > 0) {
			return longObjectMethod(n, ll - 1) + 1;
		}
		else {
			return n;
		}
	}

	public static abstract class JavaPrimitiveTwoArgFuncObject {
		public abstract long call(JavaPrimitiveTwoArgFuncObject arg1, long arg2);
	}

	public static class JavaPrimitiveTwoArgFuncObjectImpl extends JavaPrimitiveTwoArgFuncObject {

		private final long n;

		public JavaPrimitiveTwoArgFuncObjectImpl(long n) {
			this.n = n;
		}

		@Override
		public long call(JavaPrimitiveTwoArgFuncObject f, long l) {
			if (l > 0) {
				return f.call(f, l - 1) + 1;
			}
			else {
				return n;
			}
		}

	}

	public static abstract class JavaTwoArgFuncObject {
		public abstract Object call(Object arg1, Object arg2);
	}

	public static class JavaTwoArgFuncObjectImpl extends JavaTwoArgFuncObject {

		private final Long n;

		public JavaTwoArgFuncObjectImpl(long n) {
			this.n = n;
		}

		@Override
		public Object call(Object arg1, Object arg2) {
			JavaTwoArgFuncObject f = (JavaTwoArgFuncObject) arg1;
			long l = ((Number) arg2).longValue();

			if (l > 0) {
				Object r = f.call(f, l - 1);
				return ((Number)r).longValue() + 1;
			}
			else {
				return n;
			}
		}

	}

	public static abstract class JavaVarargFuncObject {
		public abstract Object call(Object... args);
	}

	public static class JavaVarargFuncObjectImpl extends JavaVarargFuncObject {

		private final Long n;

		public JavaVarargFuncObjectImpl(long n) {
			this.n = n;
		}

		@Override
		public Object call(Object... args) {
			if (args.length < 2) {
				throw new IllegalArgumentException();
			}

			JavaVarargFuncObject f = (JavaVarargFuncObject) args[0];
			long l = ((Number) args[1]).longValue();

			if (l > 0) {
				return ((Number) f.call(f, l - 1)).longValue() + 1;
			}
			else {
				return n;
			}
		}

	}


	@Benchmark
	public void _0_0_primitiveMethod() {
		long result = primitiveMethod(100, 20);
		assertEquals(result, 120L);
	}

	@Benchmark
	public void _0_1_objectMethod() {
		Object result = objectMethod(100L, 20L);
		assertEquals(result, 120L);
	}

	@Benchmark
	public void _0_2_numberObjectMethod() {
		Number result = numberObjectMethod(100L, 20L);
		assertEquals(result, 120L);
	}

	@Benchmark
	public void _0_3_longObjectMethod() {
		Long result = longObjectMethod(100L, 20L);
		assertEquals(result, 120L);
	}

	@Benchmark
	public void _0_4_javaPrimitiveFunctionObject() {
		JavaPrimitiveTwoArgFuncObject f = new JavaPrimitiveTwoArgFuncObjectImpl(100);
		long result = f.call(f, 20);
		assertEquals(result, 120L);
	}

	@Benchmark
	public void _0_5_javaGenericTwoArgFunctionObject() {
		JavaTwoArgFuncObject f = new JavaTwoArgFuncObjectImpl(100);
		Object result = f.call(f, 20);
		assertEquals(result, 120L);
	}

	@Benchmark
	public void _0_6_javaVarargFunctionObject() {
		JavaVarargFuncObject f = new JavaVarargFuncObjectImpl(100);
		Object result = f.call(f, 20);
		assertEquals(result, 120L);
	}


	public static class RecursiveInvokeFunc extends Function2 {

		private final Long n;

		public RecursiveInvokeFunc(long n) {
			this.n = n;
		}

		@Override
		public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2) throws ControlThrowable {
			Invokable f = (Invokable) arg1;
			long l = ((Number) arg2).longValue();
			if (l > 0) {
				f.invoke(state, result, f, l - 1);
				Number m = (Number) result._0();
				result.setTo(m.longValue() + 1);
			}
			else {
				result.setTo(n);
			}
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Object suspendedState) throws ControlThrowable {
			throw new UnsupportedOperationException();
		}

	}

	public static class RecursiveCallFunc extends Function2 {

		private final Long n;

		public RecursiveCallFunc(long n) {
			this.n = n;
		}

		private void run(LuaState state, ObjectSink result, int pc, Object r_0, Object r_1) throws ControlThrowable {
			switch (pc) {
				case 0:
				case 1:
					long l = ((Number) r_1).longValue();
					if (l > 0) {
						Dispatch.call(state, result, r_0, r_0, l - 1);
						Number m = (Number) result._0();
						result.setTo(m.longValue() + 1);
					}
					else {
						result.setTo(n);
					}
			}
		}

		@Override
		public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2) throws ControlThrowable {
			run(state, result, 0, arg1, arg2);
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Object suspendedState) throws ControlThrowable {
			throw new UnsupportedOperationException();
		}

	}

	public static class TailCallFunc extends Function3 {

		private final Long n;

		public TailCallFunc(long n) {
			this.n = n;
		}

		private void run(LuaState state, ObjectSink result, int pc, Object r_0, Object r_1, Object r_2) {
			switch (pc) {
				case 0:
				case 1:
					long l = ((Number) r_1).longValue();
					long acc = ((Number) r_2).longValue();
					if (l > 0) {
						result.tailCall(r_0, r_0, l - 1, acc + 1);
					}
					else {
						result.setTo(acc + n);
					}
			}
		}

		@Override
		public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
			run(state, result, 0, arg1, arg2, arg3);
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Object suspendedState) throws ControlThrowable {
			throw new UnsupportedOperationException();
		}

	}

	public static class SelfRecursiveTailCallFunc extends Function2 {

		private final Long n;

		public SelfRecursiveTailCallFunc(long n) {
			this.n = n;
		}

		private void run(LuaState state, ObjectSink result, int pc, Object r_0, Object r_1) throws ControlThrowable {
			switch (pc) {
				case 0:
				case 1:
					long l = ((Number) r_0).longValue();
					long acc = ((Number) r_1).longValue();
					if (l > 0) {
						result.tailCall(this, l - 1, acc + 1);
					}
					else {
						result.setTo(acc + n);
					}
			}
		}

		@Override
		public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2) throws ControlThrowable {
			run(state, result, 0, arg1, arg2);
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Object suspendedState) throws ControlThrowable {
			throw new UnsupportedOperationException();
		}

	}

	@Benchmark
	public void _1_1_recursiveInvoke(DummyLuaState luaState) throws ControlThrowable {
		Invokable f = new RecursiveInvokeFunc(100);
		ObjectSink result = newSink();
		f.invoke(luaState, result, f, 20);
		assertEquals(result._0(), 120L);
	}

	@Benchmark
	public void _1_2_recursiveCall(DummyLuaState luaState) throws ControlThrowable {
		Invokable f = new RecursiveCallFunc(100);
		ObjectSink result = newSink();
		Dispatch.call(luaState, result, f, f, 20);
		assertEquals(result._0(), 120L);
	}

	@Benchmark
	public void _1_3_tailCall(DummyLuaState luaState) throws ControlThrowable {
		Invokable f = new TailCallFunc(100);
		ObjectSink result = newSink();
		Dispatch.call(luaState, result, f, f, 20, 0);
		assertEquals(result._0(), 120L);
	}

	@Benchmark
	public void _1_4_selfRecursiveTailCall(DummyLuaState luaState) throws ControlThrowable {
		Invokable f = new SelfRecursiveTailCallFunc(100);
		ObjectSink result = newSink();
		Dispatch.call(luaState, result, f, 20, 0);
		assertEquals(result._0(), 120L);
	}

}
