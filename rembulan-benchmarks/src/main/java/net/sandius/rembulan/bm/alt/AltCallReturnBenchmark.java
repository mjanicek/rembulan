package net.sandius.rembulan.bm.alt;

import net.sandius.rembulan.core.alt.RetFunc;
import net.sandius.rembulan.util.Ptr;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import static net.sandius.rembulan.util.Assertions.assertEquals;

@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 20)
public class AltCallReturnBenchmark {

	public static abstract class JavaArraysFunc {
		public abstract Object[] call(Object[] args);
	}

	public static class JavaArraysFuncImpl extends JavaArraysFunc {

		private final Long n;

		public JavaArraysFuncImpl(long n) {
			this.n = n;
		}

		@Override
		public Object[] call(Object[] args) {
			JavaArraysFunc f = (JavaArraysFunc) args[0];
			long l = ((Number) args[1]).longValue();

			if (l > 0) {
				Object[] result = f.call(new Object[] { f, l - 1 });
				Number m = (Number) result[0];

				return new Object[] { m.longValue() + 1 };
			}
			else {
				return new Object[] { n };
			}
		}
	}

	@Benchmark
	public void _0_javaResultInReturnValue() {
		JavaArraysFunc f = new JavaArraysFuncImpl(100);

		Object[] result = f.call(new Object[] { f, 20 });

		assertEquals(result[0], 120L);
	}

	public static abstract class JavaArraysVoidRetFunc {
		public abstract void call(Object[] args, Ptr<Object[]> result);
	}

	public static class JavaArraysVoidRetFuncImpl_PtrAlloc extends JavaArraysVoidRetFunc {

		private final Long n;

		public JavaArraysVoidRetFuncImpl_PtrAlloc(long n) {
			this.n = n;
		}

		@Override
		public void call(Object[] args, Ptr<Object[]> result) {
			JavaArraysVoidRetFunc f = (JavaArraysVoidRetFunc) args[0];
			long l = ((Number) args[1]).longValue();

			if (l > 0) {
				Ptr<Object[]> callResult = new Ptr<Object[]>();

				f.call(new Object[] { f, l - 1 }, callResult);
				Number m = (Number) callResult.getAndClear()[0];

				result.set(new Object[] { m.longValue() + 1});
			}
			else {
				result.set(new Object[] { n });
			}
		}
	}

	public static class JavaArraysVoidRetFuncImpl_PtrReuse extends JavaArraysVoidRetFunc {

		private final Long n;

		public JavaArraysVoidRetFuncImpl_PtrReuse(long n) {
			this.n = n;
		}

		@Override
		public void call(Object[] args, Ptr<Object[]> result) {
			JavaArraysVoidRetFunc f = (JavaArraysVoidRetFunc) args[0];
			long l = ((Number) args[1]).longValue();

			if (l > 0) {
				f.call(new Object[] { f, l - 1 }, result);
				Number m = (Number) result.getAndClear()[0];

				result.set(new Object[] { m.longValue() + 1});
			}
			else {
				result.set(new Object[] { n });
			}
		}
	}

	@Benchmark
	public void _1_1_javaResultInArgument_newPtrAlloc() {
		JavaArraysVoidRetFunc f = new JavaArraysVoidRetFuncImpl_PtrAlloc(100);

		Ptr<Object[]> result = new Ptr<Object[]>();
		f.call(new Object[] { f, 20 }, result);

		assertEquals(result.get()[0], 120L);
	}

	@Benchmark
	public void _1_2_javaResultInArgument_ptrReuse() {
		JavaArraysVoidRetFunc f = new JavaArraysVoidRetFuncImpl_PtrReuse(100);

		Ptr<Object[]> result = new Ptr<Object[]>();
		f.call(new Object[] { f, 20 }, result);

		assertEquals(result.get()[0], 120L);
	}

	public static class RetFuncImpl extends RetFunc._2 {

		private final Long n;

		public RetFuncImpl(long n) {
			this.n = n;
		}

		@Override
		public Object[] call(Object a, Object b) {
			RetFunc f = (RetFunc) a;
			long l = ((Number) b).longValue();
			if (l > 0) {
				Object[] result = f.call(f, l - 1);
				Number m = (Number) result[0];

				return new Object[] { m.longValue() + 1 };
			}
			else {
				return new Object[] { n };
			}
		}

	}

	@Benchmark
	public void _2_1_retFunc() {
		RetFunc f = new RetFuncImpl(100);
		Object[] result = f.call(f, 20);
		assertEquals(result[0], 120L);
	}

}
