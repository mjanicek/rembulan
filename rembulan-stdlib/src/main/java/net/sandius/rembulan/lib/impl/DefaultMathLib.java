/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.ExecutionContext;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.MathLib;
import net.sandius.rembulan.runtime.Dispatch;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.UnresolvedControlThrowable;
import net.sandius.rembulan.util.Check;

import java.util.Random;

public class DefaultMathLib extends MathLib {

	private final LuaFunction _random;
	private final LuaFunction _randomseed;

	public DefaultMathLib(Random random) {
		Check.notNull(random);
		this._random = new Rand(random);
		this._randomseed = new RandSeed(random);
	}

	public DefaultMathLib() {
		this(new Random());
	}

	@Override
	public LuaFunction _abs() {
		return Abs.INSTANCE;
	}

	@Override
	public LuaFunction _acos() {
		return ACos.INSTANCE;
	}

	@Override
	public LuaFunction _asin() {
		return ASin.INSTANCE;
	}

	@Override
	public LuaFunction _atan() {
		return ATan.INSTANCE;
	}

	@Override
	public LuaFunction _ceil() {
		return Ceil.INSTANCE;
	}

	@Override
	public LuaFunction _cos() {
		return Cos.INSTANCE;
	}

	@Override
	public LuaFunction _deg() {
		return Deg.INSTANCE;
	}

	@Override
	public LuaFunction _exp() {
		return Exp.INSTANCE;
	}

	@Override
	public LuaFunction _floor() {
		return Floor.INSTANCE;
	}

	@Override
	public LuaFunction _fmod() {
		return FMod.INSTANCE;
	}

	@Override
	public Double _huge() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public LuaFunction _log() {
		return Log.INSTANCE;
	}

	@Override
	public LuaFunction _max() {
		return MaxMin.MAX_INSTANCE;
	}

	@Override
	public Long _maxinteger() {
		return Long.MAX_VALUE;
	}

	@Override
	public LuaFunction _min() {
		return MaxMin.MIN_INSTANCE;
	}

	@Override
	public Long _mininteger() {
		return Long.MIN_VALUE;
	}

	@Override
	public LuaFunction _modf() {
		return ModF.INSTANCE;
	}

	@Override
	public Double _pi() {
		return Math.PI;
	}

	@Override
	public LuaFunction _rad() {
		return Rad.INSTANCE;
	}

	@Override
	public LuaFunction _random() {
		return _random;
	}

	@Override
	public LuaFunction _randomseed() {
		return _randomseed;
	}

	@Override
	public LuaFunction _sin() {
		return Sin.INSTANCE;
	}

	@Override
	public LuaFunction _sqrt() {
		return Sqrt.INSTANCE;
	}

	@Override
	public LuaFunction _tan() {
		return Tan.INSTANCE;
	}

	@Override
	public LuaFunction _tointeger() {
		return ToInteger.INSTANCE;
	}

	@Override
	public LuaFunction _type() {
		return Type.INSTANCE;
	}

	@Override
	public LuaFunction _ult() {
		return ULt.INSTANCE;
	}

	public static abstract class AbstractMathFunction1 extends AbstractLibFunction {

		protected abstract Number op(double x);

		protected Number op(long x) {
			return op((double) x);
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Number x = args.nextNumber();
			Number result = x instanceof Float || x instanceof Double ? op(x.doubleValue()) : op(x.longValue());
			context.getReturnBuffer().setTo(result);
		}

	}

	public static class Abs extends AbstractMathFunction1 {

		public static final Abs INSTANCE = new Abs();

		@Override
		protected String name() {
			return "abs";
		}

		@Override
		protected Number op(double x) {
			return Math.abs(x);
		}

		@Override
		protected Number op(long x) {
			return Math.abs(x);
		}

	}

	public static class ACos extends AbstractMathFunction1 {

		public static final ACos INSTANCE = new ACos();

		@Override
		protected String name() {
			return "acos";
		}

		@Override
		protected Number op(double x) {
			return Math.acos(x);
		}

	}

	public static class ASin extends AbstractMathFunction1 {

		public static final ASin INSTANCE = new ASin();

		@Override
		protected String name() {
			return "asin";
		}

		@Override
		protected Number op(double x) {
			return Math.asin(x);
		}

	}

	public static class ATan extends AbstractMathFunction1 {

		public static final ATan INSTANCE = new ATan();

		@Override
		protected String name() {
			return "atan";
		}

		@Override
		protected Number op(double x) {
			return Math.atan(x);
		}

	}

	public static class Ceil extends AbstractMathFunction1 {

		public static final Ceil INSTANCE = new Ceil();

		@Override
		protected String name() {
			return "ceil";
		}

		@Override
		protected Number op(double x) {
			double d = Math.ceil(x);
			long l = (long) d;
			return d == (double) l ? l : d;
		}

		@Override
		protected Number op(long x) {
			return x;
		}

	}

	public static class Cos extends AbstractMathFunction1 {

		public static final Cos INSTANCE = new Cos();

		@Override
		protected String name() {
			return "cos";
		}

		@Override
		protected Number op(double x) {
			return Math.cos(x);
		}

	}

	public static class Deg extends AbstractMathFunction1 {

		public static final Deg INSTANCE = new Deg();

		@Override
		protected String name() {
			return "deg";
		}

		@Override
		protected Number op(double x) {
			return Math.toDegrees(x);
		}

	}

	public static class Exp extends AbstractMathFunction1 {

		public static final Exp INSTANCE = new Exp();

		@Override
		protected String name() {
			return "exp";
		}

		@Override
		protected Number op(double x) {
			return Math.exp(x);
		}

	}

	public static class Floor extends AbstractMathFunction1 {

		public static final Floor INSTANCE = new Floor();

		@Override
		protected String name() {
			return "floor";
		}

		@Override
		protected Number op(double x) {
			double d = Math.floor(x);
			long l = (long) d;
			// Note: explicit type annotations are necessary: per JLS 15.25, the type of the
			// expression would be *double* (not java.lang.Number) without them
			return d == (double) l ? (Number) l : (Number) d;
		}

		@Override
		protected Number op(long x) {
			return x;
		}

	}

	public static class FMod extends AbstractLibFunction {

		public static final FMod INSTANCE = new FMod();

		@Override
		protected String name() {
			return "fmod";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Number x = args.nextNumber();
			Number y = args.nextNumber();

			final Number result;

			if (x instanceof Float || x instanceof Double
					|| y instanceof Float || y instanceof Double) {

				result = Math.IEEEremainder(x.doubleValue(), y.doubleValue());
			}
			else {
				long xi = x.longValue();
				long yi = y.longValue();

				if (yi != 0) {
					result = xi % yi;
				}
				else {
					throw new BadArgumentException(2, name(), "zero");
				}
			}

			context.getReturnBuffer().setTo(result);
		}

	}

	public static class Log extends AbstractLibFunction {

		public static final Log INSTANCE = new Log();

		@Override
		protected String name() {
			return "log";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Number x = args.nextNumber();
			double ln = Math.log(x.doubleValue());
			final double result;

			if (args.hasNext()) {
				// explicit base
				double base = args.nextNumber().doubleValue();
				result = ln / Math.log(base);
			}
			else {
				// no base specified
				result = ln;
			}

			context.getReturnBuffer().setTo(result);
		}

	}

	public static class MaxMin extends AbstractLibFunction {

		private final boolean isMax;

		public static final MaxMin MAX_INSTANCE = new MaxMin(true);
		public static final MaxMin MIN_INSTANCE = new MaxMin(false);

		public MaxMin(boolean isMax) {
			this.isMax = isMax;
		}

		private static class State {

			public final Object[] args;
			public final int idx;
			public final Object best;

			public State(Object[] args, int idx, Object best) {
				this.args = args;
				this.idx = idx;
				this.best = best;
			}

		}

		@Override
		protected String name() {
			return isMax ? "max" : "min";
		}

		private void run(ExecutionContext context, Object[] args, int idx, Object best) throws ResolvedControlThrowable {
			for ( ; idx < args.length; idx++) {
				Object o = args[idx];

				try {
					if (isMax) {
						Dispatch.lt(context, best, o);
					}
					else {
						Dispatch.lt(context, o, best);
					}
				}
				catch (UnresolvedControlThrowable ct) {
					throw ct.push(this, new State(args, idx, best));
				}

				if (Conversions.booleanValueOf(context.getReturnBuffer().get0())) {
					best = o;
				}
			}

			// we're done
			context.getReturnBuffer().setTo(best);
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object initial = args.nextAny();
			run(context, args.getAll(), 1, initial);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			State ss = (State) suspendedState;

			Object[] args = ss.args;
			int idx = ss.idx;
			Object best = ss.best;

			// best <> args[idx] comparison has just finished
			if (Conversions.booleanValueOf(context.getReturnBuffer().get0())) {
				best = args[idx];
			}

			run(context, args, idx + 1, best);
		}

	}

	public static class ModF extends AbstractLibFunction {

		public static final ModF INSTANCE = new ModF();

		@Override
		protected String name() {
			return "modf";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Number x = args.nextNumber();

			final Number intPart;
			final Number fltPart;

			double d = x.doubleValue();

			if (d == d) {
				double dd = d < 0 ? Math.ceil(d) : Math.floor(d);
				long l = (long) dd;
				if (dd == (double) l) {
					intPart = l;
					fltPart = d - l;
				}
				else {
					intPart = x;
					fltPart = 0.0;
				}
			}
			else {
				// NaN
				intPart = x;
				fltPart = x;
			}

			context.getReturnBuffer().setTo(intPart, fltPart);
		}

	}

	public static class Rad extends AbstractMathFunction1 {

		public static final Rad INSTANCE = new Rad();

		@Override
		protected String name() {
			return "rad";
		}

		@Override
		protected Number op(double x) {
			return Math.toRadians(x);
		}

	}

	public static class Rand extends AbstractLibFunction {

		protected final Random random;

		public Rand(Random random) {
			this.random = Check.notNull(random);
		}

		@Override
		protected String name() {
			return "random";
		}

		// return a long in the range [0, n)
		protected long nextLong(long n) {
			Check.nonNegative(n);
			if (n <= Integer.MAX_VALUE) {
				return random.nextInt((int) n);
			}
			else {
				long bits;
				long val;
				do {
					bits = random.nextLong() & Long.MAX_VALUE;
					val = bits % n;
				} while (bits - val + (n - 1) < 0);
				return val;
			}
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			final Number result;

			if (!args.hasNext()) {
				// float in the range [0.0, 1.0)
				result = random.nextDouble();
			}
			else {
				long m = args.nextInteger();

				if (!args.hasNext()) {
					// integer in the range [1, m]
					if (m < 1) {
						throw new BadArgumentException(1, name(), "interval is empty");
					}
					result = 1L + nextLong(m);
				}
				else {
					// integer in the range [m, n]
					long n = args.nextInteger();

					if (n < m) {
						throw new BadArgumentException(1, name(), "interval is empty");
					}

					long limit = n - m + 1;  // including the upper bound

					if (limit <= 0) {
						throw new BadArgumentException(1, name(), "interval too large");
					}

					result = m + nextLong(limit);
				}
			}

			context.getReturnBuffer().setTo(result);
		}

	}

	public static class RandSeed extends AbstractLibFunction {

		protected final Random random;

		public RandSeed(Random random) {
			this.random = Check.notNull(random);
		}

		@Override
		protected String name() {
			return "randomseed";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Number arg = args.nextNumber();

			long seed = arg instanceof Double || arg instanceof Float
					? Double.doubleToLongBits(arg.doubleValue())
					: arg.longValue();

			random.setSeed(seed);

			context.getReturnBuffer().setTo();
		}

	}

	public static class Sin extends AbstractMathFunction1 {

		public static final Sin INSTANCE = new Sin();

		@Override
		protected String name() {
			return "sin";
		}

		@Override
		protected Number op(double x) {
			return Math.sin(x);
		}

	}

	public static class Sqrt extends AbstractMathFunction1 {

		public static final Sqrt INSTANCE = new Sqrt();

		@Override
		protected String name() {
			return "sqrt";
		}

		@Override
		protected Number op(double x) {
			return Math.sqrt(x);
		}

	}

	public static class Tan extends AbstractMathFunction1 {

		public static final Tan INSTANCE = new Tan();

		@Override
		protected String name() {
			return "tan";
		}

		@Override
		protected Number op(double x) {
			return Math.tan(x);
		}

	}

	public static class ToInteger extends AbstractLibFunction {

		public static final ToInteger INSTANCE = new ToInteger();

		@Override
		protected String name() {
			return "tointeger";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object x = args.nextAny();
			context.getReturnBuffer().setTo(Conversions.integerValueOf(x));
		}

	}

	public static class Type extends AbstractLibFunction {

		public static final Type INSTANCE = new Type();

		@Override
		protected String name() {
			return "type";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object x = args.nextAny();

			String result = x instanceof Number
					? (x instanceof Float || x instanceof Double
							? "float"
							: "integer")
					: null;

			context.getReturnBuffer().setTo(result);
		}

	}

	public static class ULt extends AbstractLibFunction {

		public static final ULt INSTANCE = new ULt();

		@Override
		protected String name() {
			return "ult";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			long x = args.nextInteger();
			long y = args.nextInteger();
			context.getReturnBuffer().setTo((x - y) < 0);
		}

	}

}
