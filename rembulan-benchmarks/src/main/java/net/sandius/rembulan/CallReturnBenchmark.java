package net.sandius.rembulan;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.FixedSizeRegisters;
import net.sandius.rembulan.core.IllegalOperationAttemptException;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.LuaType;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.ObjectStack;
import net.sandius.rembulan.core.ReturnTarget;
import net.sandius.rembulan.util.ObjectSink;
import net.sandius.rembulan.util.Ptr;
import net.sandius.rembulan.util.QuintupleCachingObjectSink;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import static net.sandius.rembulan.Util.assertEquals;

@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 20)
public class CallReturnBenchmark {

	public static ObjectSink newSink() {
		return new QuintupleCachingObjectSink();
	}

	public static abstract class JavaTwoArgFunc {
		public abstract Object call(Object arg1, Object arg2);
	}

	public static class JavaTwoArgFuncImpl extends JavaTwoArgFunc {

		private final Long n;

		public JavaTwoArgFuncImpl(long n) {
			this.n = n;
		}

		@Override
		public Object call(Object arg1, Object arg2) {
			JavaTwoArgFunc f = (JavaTwoArgFunc) arg1;
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

	@Benchmark
	public void _0_javaTwoArgCallMethod() {
		JavaTwoArgFunc f = new JavaTwoArgFuncImpl(100);

		Object result = f.call(f, 20);

		assertEquals(result, 120L);
	}

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
	public void _1_javaResultInReturnValue() {
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
	public void _2_1_javaResultInArgument_newPtrAlloc() {
		JavaArraysVoidRetFunc f = new JavaArraysVoidRetFuncImpl_PtrAlloc(100);

		Ptr<Object[]> result = new Ptr<Object[]>();
		f.call(new Object[] { f, 20 }, result);

		assertEquals(result.get()[0], 120L);
	}

	@Benchmark
	public void _2_2_javaResultInArgument_ptrReuse() {
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
	public void _3_1_retFunc() {
		RetFunc f = new RetFuncImpl(100);
		Object[] result = f.call(f, 20);
		assertEquals(result[0], 120L);
	}

	public static class RecursiveInvokeFunc extends AbstractFunc2 {

		private final Long n;

		public RecursiveInvokeFunc(long n) {
			this.n = n;
		}

		@Override
		public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2) throws ControlThrowable {
			Func f = (Func) arg1;
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

	public static Func callTarget(LuaState state, Object target) {
		if (target instanceof Func) {
			return (Func) target;
		}
		else {
			Object handler = Metatables.getMetamethod(state, Metatables.MT_CALL, target);

			if (handler instanceof Func) {
				return (Func) handler;
			}
			else {
				throw new IllegalOperationAttemptException("call", LuaType.typeOf(target).name);
			}
		}
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target) throws ControlThrowable {
		Func fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result);
		else fn.invoke(state, result, target);
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object arg1) throws ControlThrowable {
		Func fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result, arg1);
		else fn.invoke(state, result, target, arg1);
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2) throws ControlThrowable {
		Func fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result, arg1, arg2);
		else fn.invoke(state, result, target, arg1, arg2);
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		Func fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result, arg1, arg2, arg3);
		else fn.invoke(state, result, target, arg1, arg2, arg3);
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		Func fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result, arg1, arg2, arg3, arg4);
		else fn.invoke(state, result, target, arg1, arg2, arg3, arg4);
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		Func fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result, arg1, arg2, arg3, arg4, arg5);
		else fn.invoke(state, result, new Object[] { target, arg1, arg2, arg3, arg4, arg5 });
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object[] args) throws ControlThrowable {
		Func fn = callTarget(state, target);
		if (fn == target) {
			fn.invoke(state, result, args);
		}
		else {
			Object[] mtArgs = new Object[args.length + 1];
			mtArgs[0] = target;
			System.arraycopy(args, 0, mtArgs, 1, args.length);
			fn.invoke(state, result, mtArgs);
		}
	}

	public static void evaluateTailCalls(LuaState state, ObjectSink r) throws ControlThrowable {
		while (r.isTailCall()) {
			switch (r.size()) {
				case 0: throw new IllegalStateException();
				case 1: mt_invoke(state, r, r._0()); break;
				case 2: mt_invoke(state, r, r._0(), r._1()); break;
				case 3: mt_invoke(state, r, r._0(), r._1(), r._2()); break;
				case 4: mt_invoke(state, r, r._0(), r._1(), r._2(), r._3()); break;
				case 5: mt_invoke(state, r, r._0(), r._1(), r._2(), r._3(), r._4()); break;
				default: mt_invoke(state, r, r._0(), r.tailAsArray()); break;
			}
		}
	}

	public static void call(LuaState state, ObjectSink result, Object target) throws ControlThrowable {
		mt_invoke(state, result, target);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object arg1) throws ControlThrowable {
		mt_invoke(state, result, target, arg1);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2) throws ControlThrowable {
		mt_invoke(state, result, target, arg1, arg2);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		mt_invoke(state, result, target, arg1, arg2, arg3);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		mt_invoke(state, result, target, arg1, arg2, arg3, arg4);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		mt_invoke(state, result, target, arg1, arg2, arg3, arg4, arg5);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object[] args) throws ControlThrowable {
		mt_invoke(state, result, target, args);
		evaluateTailCalls(state, result);
	}

	public static class RecursiveCallFunc extends AbstractFunc2 {

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
						call(state, result, r_0, r_0, l - 1);
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

	public static class TailCallFunc extends AbstractFunc3 {

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

	public static class SelfRecursiveTailCallFunc extends AbstractFunc2 {

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
	public void _4_1_recursiveInvoke(DummyLuaState luaState) throws ControlThrowable {
		Func f = new RecursiveInvokeFunc(100);
		ObjectSink result = newSink();
		f.invoke(luaState, result, f, 20);
		assertEquals(result._0(), 120L);
	}

	@Benchmark
	public void _4_2_recursiveCall(DummyLuaState luaState) throws ControlThrowable {
		Func f = new RecursiveCallFunc(100);
		ObjectSink result = newSink();
		call(luaState, result, f, f, 20);
		assertEquals(result._0(), 120L);
	}

	@Benchmark
	public void _4_3_tailCall(DummyLuaState luaState) throws ControlThrowable {
		Func f = new TailCallFunc(100);
		ObjectSink result = newSink();
		call(luaState, result, f, f, 20, 0);
		assertEquals(result._0(), 120L);
	}

	@Benchmark
	public void _4_4_selfRecursiveTailCall(DummyLuaState luaState) throws ControlThrowable {
		Func f = new SelfRecursiveTailCallFunc(100);
		ObjectSink result = newSink();
		call(luaState, result, f, 20, 0);
		assertEquals(result._0(), 120L);
	}

	public static abstract class ViewFunc {
		public abstract void call(ObjectStack.View self, ObjectStack.View ret);
	}

	public static class ViewFuncImpl extends ViewFunc {

		private final Long n;

		public ViewFuncImpl(long n) {
			this.n = n;
		}

		@Override
		public void call(ObjectStack.View self, ObjectStack.View ret) {
			Object r_0, r_1, r_2;
			int top;

			// load registers
			r_0 = self.get(0);
			r_1 = self.get(1);
			r_2 = self.get(2);
			top = self.getTop();

			ViewFunc f = (ViewFunc) r_0;
			long l = ((Number) r_1).longValue();

			if (l > 0) {

				r_2 = l - 1;
				r_1 = r_0;

				ObjectStack.View callSelf = self.from(1);
				ObjectStack.View callRet = self.from(0);

				callSelf.set(0, r_1);
				callSelf.set(1, r_2);
				callSelf.setTop(2);

				f.call(callSelf, callRet);

				r_0 = self.get(0);
				r_1 = self.get(1);
				r_2 = self.get(2);
				top = self.getTop();

				Number m = (Number) r_0;

				r_0 = m.longValue() + 1;

				ret.set(0, r_0);
				ret.setTop(1);
			}
			else {
				ret.set(0, n);
				ret.setTop(1);
			}
		}

	}

	@Benchmark
	public void _5_sharedStackWithViews(RegistersBenchmark.ObjectStackHolder osh) {
		ObjectStack os = osh.objectStack;
		ViewFunc f = new ViewFuncImpl(100);
		ObjectStack.View root = os.rootView();
		root.set(0, f);
		root.set(1, 20);
		root.setTop(2);

		f.call(root, root);

		assertEquals(os.get(0), 120L);
	}

	public static abstract class DirectFunc {
		public abstract void call(ObjectStack objectStack, int base, int ret);
	}

	public static class DirectFuncImpl extends DirectFunc {

		private final Long n;

		public DirectFuncImpl(long n) {
			this.n = n;
		}

		@Override
		public void call(ObjectStack objectStack, int base, int ret) {
			Object r_0, r_1, r_2;
			int top;

			r_0 = objectStack.get(base + 0);
			r_1 = objectStack.get(base + 1);
			r_2 = objectStack.get(base + 2);
			top = objectStack.getTop() - base;

			DirectFunc f = (DirectFunc) r_0;
			long l = ((Number) r_1).longValue();

			if (l > 0) {

				r_2 = l - 1;
				r_1 = r_0;

				objectStack.set(base + 1, r_1);
				objectStack.set(base + 2, r_2);
				objectStack.setTop(base + 3);

				f.call(objectStack, base + 1, base);

				r_0 = objectStack.get(base + 0);
				r_1 = objectStack.get(base + 1);
				r_2 = objectStack.get(base + 2);
				top = objectStack.getTop() - base;

				Number m = (Number) r_0;

				r_0 = m.longValue() + 1;

				objectStack.set(ret + 0, r_0);
				objectStack.setTop(ret + 1);
			}
			else {
				objectStack.set(ret + 0, n);
				objectStack.setTop(ret + 1);
			}

		}

	}

	@Benchmark
	public void _6_directStackManipulation(RegistersBenchmark.ObjectStackHolder osh) {
		ObjectStack os = osh.objectStack;
		DirectFunc f = new DirectFuncImpl(100);
		os.set(0, f);
		os.set(1, 20);
		os.setTop(2);

		f.call(os, 0, 0);

		assertEquals(os.get(0), 120L);
	}

	public static abstract class AllocFunc {
		public abstract FixedSizeRegisters newRegisters();
		public abstract void call(FixedSizeRegisters self, ReturnTarget ret);
	}

	public class AllocFuncImpl extends AllocFunc {

		private final Long n;

		public AllocFuncImpl(long n) {
			this.n = n;
		}

		@Override
		public FixedSizeRegisters newRegisters() {
			return new FixedSizeRegisters(3);
		}

		@Override
		public void call(FixedSizeRegisters self, ReturnTarget ret) {
			Object r_0, r_1, r_2;
			int top;

			r_0 = self.get(0);
			r_1 = self.get(1);
			r_2 = self.get(2);
			top = self.getTop();

			AllocFunc f = (AllocFunc) r_0;
			long l = ((Number) r_1).longValue();

			if (l > 0) {

				r_2 = l - 1;
				r_1 = r_0;

				FixedSizeRegisters callSelf = f.newRegisters();
				callSelf.push(r_1);
				callSelf.push(r_2);

				f.call(callSelf, self.returnTargetFrom(0));

				r_0 = self.get(0);
				r_1 = self.get(1);
				r_2 = self.get(2);
				top = self.getTop();

				Number m = (Number) r_0;

				r_0 = m.longValue() + 1;

				ret.begin();
				ret.push(r_0);
				ret.end();
			}
			else {
				ret.begin();
				ret.push(n);
				ret.end();
			}

		}

	}

	@Benchmark
	public void _7_perCallRegisterAllocationWithPushInterface() {
		AllocFunc f = new AllocFuncImpl(100);

		FixedSizeRegisters out = new FixedSizeRegisters(1);

		FixedSizeRegisters regs = f.newRegisters();
		regs.push(f);
		regs.push(20);

		f.call(regs, out.returnTargetFrom(0));

		assertEquals(out.get(0), 120L);
	}


}
