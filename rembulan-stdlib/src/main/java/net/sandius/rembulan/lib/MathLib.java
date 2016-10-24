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
 *
 * --
 * Portions of this file are licensed under the Lua license. For Lua
 * licensing details, please visit
 *
 *     http://www.lua.org/license.html
 *
 * Copyright (C) 1994-2016 Lua.org, PUC-Rio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.sandius.rembulan.lib;

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.runtime.Dispatch;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.UnresolvedControlThrowable;
import net.sandius.rembulan.util.Check;

import java.util.Objects;
import java.util.Random;

/**
 * This library provides basic mathematical functions. It provides all its functions and constants
 * inside the table {@code math}. Functions with the annotation "integer/float" give integer
 * results for integer arguments and float results for float (or mixed) arguments. Rounding
 * functions ({@link #ceil() <code>math.ceil</code>}, {@link #floor() <code>math.floor</code>},
 * and {@link #modf() <code>math.modf</code>}) return an integer when the result fits
 * in the range of an integer, or a float otherwise.
 */
public final class MathLib {

	static final LuaFunction ABS = new Abs();
	static final LuaFunction ACOS = new ACos();
	static final LuaFunction ASIN = new ASin();
	static final LuaFunction ATAN = new ATan();
	static final LuaFunction CEIL = new Ceil();
	static final LuaFunction COS = new Cos();
	static final LuaFunction DEG = new Deg();
	static final LuaFunction EXP = new Exp();
	static final LuaFunction FLOOR = new Floor();
	static final LuaFunction FMOD = new FMod();
	static final LuaFunction LOG = new Log();
	static final LuaFunction MAX = new MaxMin(true);
	static final LuaFunction MIN = new MaxMin(false);
	static final LuaFunction MODF = new ModF();
	static final LuaFunction RAD = new Rad();
	static final LuaFunction SIN = new Sin();
	static final LuaFunction SQRT = new Sqrt();
	static final LuaFunction TAN = new Tan();
	static final LuaFunction TOINTEGER = new ToInteger();
	static final LuaFunction TYPE = new Type();
	static final LuaFunction ULT = new ULt();
	
	
	/**
	 * Returns the function {@code math.abs}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.abs (x)}
	 *
	 * <p>Returns the absolute value of {@code x}. (integer/float)</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.abs} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.abs">
	 *     the Lua 5.3 Reference Manual entry for <code>math.abs</code></a>
	 */
	public static LuaFunction abs() {
		return ABS;
	}

	/**
	 * Returns the function {@code math.acos}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.acos (x)}
	 *
	 * <p>Returns the arc cosine of {@code x} (in radians).</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.acos} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.acos">
	 *     the Lua 5.3 Reference Manual entry for <code>math.acos</code></a>
	 */
	public static LuaFunction acos() {
		return ACOS;
	}

	/**
	 * Returns the function {@code math.asin}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.asin (x)}
	 *
	 * <p>Returns the arc sine of {@code x} (in radians).</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.asin} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.asin">
	 *     the Lua 5.3 Reference Manual entry for <code>math.asin</code></a>
	 */
	public static LuaFunction asin() {
		return ASIN;
	}

	/**
	 * Returns the function {@code math.atan}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.atan (x)}
	 *
	 * <p>Returns the arc tangent of {@code y/x} (in radians), but uses the signs of both
	 * parameters to find the quadrant of the result. (It also handles correctly the case
	 * of {@code x} being zero.)</p>
	 *
	 * <p>The default value for {@code x} is 1, so that the call {@code math.atan(y)} returns
	 * the arc tangent of {@code y}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.atan} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.atan">
	 *     the Lua 5.3 Reference Manual entry for <code>math.atan</code></a>
	 */
	public static LuaFunction atan() {
		return ATAN;
	}

	/**
	 * Returns the function {@code math.ceil}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.ceil (x)}
	 *
	 * <p>Returns the smallest integer larger than or equal to {@code x}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.ceil} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.ceil">
	 *     the Lua 5.3 Reference Manual entry for <code>math.ceil</code></a>
	 */
	public static LuaFunction ceil() {
		return CEIL;
	}

	/**
	 * Returns the function {@code math.cos}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.cos (x)}
	 *
	 * <p>Returns the cosine of {@code x} (assumed to be in radians).</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.cos} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.cos">
	 *     the Lua 5.3 Reference Manual entry for <code>math.cos</code></a>
	 */
	public static LuaFunction cos() {
		return COS;
	}

	/**
	 * Returns the function {@code math.deg}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.deg (x)}
	 *
	 * <p>Returns the angle {@code x} (given in radians) in degrees.</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.deg} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.deg">
	 *     the Lua 5.3 Reference Manual entry for <code>math.deg</code></a>
	 */
	public static LuaFunction deg() {
		return DEG;
	}

	/**
	 * Returns the function {@code math.exp}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.exp (x)}
	 *
	 * <p>Returns the value <i>e</i><sup>{@code x}</sup> (where <i>e</i> is the base
	 * of natural logarithms).</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.exp} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.exp">
	 *     the Lua 5.3 Reference Manual entry for <code>math.exp</code></a>
	 */
	public static LuaFunction exp() {
		return EXP;
	}

	/**
	 * Returns the function {@code math.floor}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.floor (x)}
	 *
	 * <p>Returns the largest integral value smaller than or equal to {@code x}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.floor} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.floor">
	 *     the Lua 5.3 Reference Manual entry for <code>math.floor</code></a>
	 */
	public static LuaFunction floor() {
		return FLOOR;
	}

	/**
	 * Returns the function {@code math.fmod}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.fmod (x, y)}
	 *
	 * <p>Returns the remainder of the division of {@code x} by {@code y} that rounds
	 * the quotient towards zero. (integer/float)</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.fmod} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.fmod">
	 *     the Lua 5.3 Reference Manual entry for <code>math.fmod</code></a>
	 */
	public static LuaFunction fmod() {
		return FMOD;
	}

	/**
	 * {@code math.huge}
	 *
	 * <p>The value {@code HUGE_VAL}, a value larger than or equal to any other numerical
	 * value.</p>
	 */
	public static final Double HUGE = Double.POSITIVE_INFINITY;

	/**
	 * Returns the function {@code math.log}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.log (x [, base])}
	 *
	 * <p>Returns the logarithm of {@code x} in the given base. The default for {@code base}
	 * is <i>e</i> (so that the function returns the natural logarithm of {@code x}).</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.log} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.log">
	 *     the Lua 5.3 Reference Manual entry for <code>math.log</code></a>
	 */
	public static LuaFunction log() {
		return LOG;
	}

	/**
	 * Returns the function {@code math.max}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.max (x, ···)}
	 *
	 * <p>Returns the argument with the maximum value, according to the Lua operator &lt;.
	 * (integer/float)</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.max} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.max">
	 *     the Lua 5.3 Reference Manual entry for <code>math.max</code></a>
	 */
	public static LuaFunction max() {
		return MAX;
	}

	/**
	 * An integer with the maximum value for an integer.
	 */
	public static final Long MAXINTEGER = Long.MAX_VALUE;

	/**
	 * Returns the function {@code math.min}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.min (x, ···)}
	 *
	 * <p>Returns the argument with the minimum value, according to the Lua operator &lt;.
	 * (integer/float)</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.min} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.min">
	 *     the Lua 5.3 Reference Manual entry for <code>math.min</code></a>
	 */
	public static LuaFunction min() {
		return MIN;
	}

	/**
	 * An integer with the minimum value for an integer.
	 */
	public static final Long MININTEGER = Long.MIN_VALUE;

	/**
	 * Returns the function {@code math.modf}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.modf (x)}
	 *
	 * <p>Returns the integral part of {@code x} and the fractional part of {@code x}.
	 * Its second result is always a float.</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.modf} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.modf">
	 *     the Lua 5.3 Reference Manual entry for <code>math.modf</code></a>
	 */
	public static LuaFunction modf() {
		return MODF;
	}

	/**
	 * {@code math.pi}
	 *
	 * <p>The value of &pi;.</p>
	 */
	public static final Double PI = Math.PI;

	/**
	 * Returns the function {@code math.rad}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.rad (x)}
	 *
	 * <p>Returns the angle {@code x} (given in degrees) in radians.</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.rad} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.rad">
	 *     the Lua 5.3 Reference Manual entry for <code>math.rad</code></a>
	 */
	public static LuaFunction rad() {
		return RAD;
	}

	/**
	 * Returns a function {@code math.random} that uses the argument {@code random}
	 * as a random number generator.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.random ([m [, n]])}
	 *
	 * <p>When called without arguments, returns a uniform pseudo-random real number
	 * in the range <i>[0,1)</i>. When called with an integer number {@code m},
	 * {@code math.random} returns a uniform pseudo-random integer in the range
	 * <i>[1, m]</i>. When called with two integer numbers {@code m} and {@code n},
	 * {@code math.random} returns a uniform pseudo-random integer in the range
	 * <i>[m, n]</i>.</p>
	 *
	 * <p>This function is an interface to the simple pseudo-random generator function
	 * {@code rand} provided by Standard C. (No guarantees can be given for its statistical
	 * properties.)</p>
	 * </blockquote>
	 *
	 * @param random  the random number generator, must not be {@code null}
	 * @return  the {@code math.random} function
	 *
	 * @throws NullPointerException  if {@code random} is {@code null}
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.random">
	 *     the Lua 5.3 Reference Manual entry for <code>math.random</code></a>
	 */
	public static LuaFunction random(Random random) {
		return new Rand(random);
	}

	/**
	 * Returns a function {@code math.randomseed} that sets the seed of the random
	 * generator {@code random}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.randomseed (x)}
	 *
	 * <p>Sets {@code x} as the "seed" for the pseudo-random generator: equal seeds produce
	 * equal sequences of numbers.</p>
	 * </blockquote>
	 *
	 * @param random  the random number generator, must not be {@code null}
	 * @return  the {@code math.randomseed} function
	 *
	 * @throws NullPointerException  if {@code random} is {@code null}
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.randomseed">
	 *     the Lua 5.3 Reference Manual entry for <code>math.randomseed</code></a>
	 */
	public static LuaFunction randomseed(Random random) {
		return new RandSeed(random);
	}

	/**
	 * Returns the function {@code math.sin}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.sin (x)}
	 *
	 * <p>Returns the sine of {@code x} (assumed to be in radians).</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.sin} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.sin">
	 *     the Lua 5.3 Reference Manual entry for <code>math.sin</code></a>
	 */
	public static LuaFunction sin() {
		return SIN;
	}

	/**
	 * Returns the function {@code math.sqrt}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.sqrt (x)}
	 *
	 * <p>Returns the square root of {@code x}. (You can also use the expression {@code x^0.5}
	 * to compute this value.)</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.sqrt} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.sqrt">
	 *     the Lua 5.3 Reference Manual entry for <code>math.sqrt</code></a>
	 */
	public static LuaFunction sqrt() {
		return SQRT;
	}

	/**
	 * Returns the function {@code math.tan}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.tan (x)}
	 *
	 * <p>Returns the tangent of {@code x} (assumed to be in radians).</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.tan} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.tan">
	 *     the Lua 5.3 Reference Manual entry for <code>math.tan</code></a>
	 */
	public static LuaFunction tan() {
		return TAN;
	}

	/**
	 * Returns the function {@code math.tointeger}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.tointeger (x)}
	 *
	 * <p>If the value {@code x} is convertible to an integer, returns that integer.
	 * Otherwise, returns <b>nil</b>.</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.tointeger} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.tointeger">
	 *     the Lua 5.3 Reference Manual entry for <code>math.tointeger</code></a>
	 */
	public static LuaFunction tointeger() {
		return TOINTEGER;
	}

	/**
	 * Returns the function {@code math.type}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.type (x)}
	 *
	 * <p>Returns {@code "integer"} if {@code x} is an integer, {@code "float"} if it is a float,
	 * or <b>nil</b> if {@code x} is not a number.</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.type} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.type">
	 *     the Lua 5.3 Reference Manual entry for <code>math.type</code></a>
	 */
	public static LuaFunction type() {
		return TYPE;
	}

	/**
	 * Returns the function {@code math.ult}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code math.ult (m, n)}
	 *
	 * <p>Returns a boolean, <b>true</b> if integer {@code m} is below integer {@code n} when
	 * they are compared as unsigned integers.</p>
	 * </blockquote>
	 *
	 * @return  the {@code math.ult} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-math.ult">
	 *     the Lua 5.3 Reference Manual entry for <code>math.ult</code></a>
	 */
	public static LuaFunction ult() {
		return ULT;
	}


	private MathLib() {
		// not to be instantiated
	}

	/**
	 * Installs the math library to the global environment {@code env} in the state
	 * context {@code context}, with the random number generator {@code random}.
	 *
	 * <p>{@code random} may be {@code null}, in which case the functions {@code random}
	 * and {@code randomseed} will not be installed.</p>
	 *
	 * <p>If {@code env.package.loaded} is a table, adds the library table
	 * to it with the key {@code "math"}, using raw access.</p>
	 *
	 * @param context  the state context, must not be {@code null}
	 * @param env  the global environment, must not be {@code null}
	 * @param random  the random number generator, may be {@code null}
	 *
	 * @throws NullPointerException  if {@code context} or {@code env} is {@code null}
	 */
	public static void installInto(StateContext context, Table env, Random random) {
		Objects.requireNonNull(context);
		Objects.requireNonNull(env);

		Table t = context.newTable();

		t.rawset("abs", abs());
		t.rawset("acos", acos());
		t.rawset("asin", asin());
		t.rawset("atan", atan());
		t.rawset("ceil", ceil());
		t.rawset("cos", cos());
		t.rawset("deg", deg());
		t.rawset("exp", exp());
		t.rawset("floor", floor());
		t.rawset("fmod", fmod());
		t.rawset("huge", HUGE);
		t.rawset("log", log());
		t.rawset("max", max());
		t.rawset("maxinteger", MAXINTEGER);
		t.rawset("min", min());
		t.rawset("mininteger", MININTEGER);
		t.rawset("modf", modf());
		t.rawset("pi", PI);
		t.rawset("rad", rad());
		if (random != null) t.rawset("random", MathLib.random(random));
		if (random != null) t.rawset("randomseed", randomseed(random));
		t.rawset("sin", sin());
		t.rawset("sqrt", sqrt());
		t.rawset("tan", tan());
		t.rawset("tointeger", tointeger());
		t.rawset("type", type());
		t.rawset("ult", ult());
		
		ModuleLib.install(env, "math", t);
	}

	/**
	 * Installs the math library to the global environment {@code env} in the state
	 * context {@code context}, with a new random number generator of class {@link Random}.
	 *
	 * <p>To specify the random number generator or exclude random-related functions,
	 * use {@link #installInto(StateContext, Table, Random)}.</p>
	 *
	 * @param context  the state context, must not be {@code null}
	 * @param env  the global environment, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code context} or {@code env} is {@code null}
	 */
	public static void installInto(StateContext context, Table env) {
		installInto(context, env, new Random());
	}

	static abstract class AbstractMathFunction1 extends AbstractLibFunction {

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

	static class Abs extends AbstractMathFunction1 {

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

	static class ACos extends AbstractMathFunction1 {

		@Override
		protected String name() {
			return "acos";
		}

		@Override
		protected Number op(double x) {
			return Math.acos(x);
		}

	}

	static class ASin extends AbstractMathFunction1 {

		@Override
		protected String name() {
			return "asin";
		}

		@Override
		protected Number op(double x) {
			return Math.asin(x);
		}

	}

	static class ATan extends AbstractMathFunction1 {

		@Override
		protected String name() {
			return "atan";
		}

		@Override
		protected Number op(double x) {
			return Math.atan(x);
		}

	}

	static class Ceil extends AbstractMathFunction1 {

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

	static class Cos extends AbstractMathFunction1 {

		@Override
		protected String name() {
			return "cos";
		}

		@Override
		protected Number op(double x) {
			return Math.cos(x);
		}

	}

	static class Deg extends AbstractMathFunction1 {

		@Override
		protected String name() {
			return "deg";
		}

		@Override
		protected Number op(double x) {
			return Math.toDegrees(x);
		}

	}

	static class Exp extends AbstractMathFunction1 {

		@Override
		protected String name() {
			return "exp";
		}

		@Override
		protected Number op(double x) {
			return Math.exp(x);
		}

	}

	static class Floor extends AbstractMathFunction1 {

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

	static class FMod extends AbstractLibFunction {

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

	static class Log extends AbstractLibFunction {

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

	static class MaxMin extends AbstractLibFunction {

		private final boolean isMax;

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
					throw ct.resolve(this, new State(args, idx, best));
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
			run(context, args.copyAll(), 1, initial);
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

	static class ModF extends AbstractLibFunction {

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

	static class Rad extends AbstractMathFunction1 {

		@Override
		protected String name() {
			return "rad";
		}

		@Override
		protected Number op(double x) {
			return Math.toRadians(x);
		}

	}

	static class Rand extends AbstractLibFunction {

		/**
		 * The random number generator used by this function.
		 */
		protected final Random random;

		/**
		 * Constructs a new {@code math.random} function that uses {@code random} as its
		 * random number generator.
		 *
		 * @param random  the random number generator to use, must not be {@code null}
		 *
		 * @throws NullPointerException  if {@code random} is {@code null}
		 */
		public Rand(Random random) {
			this.random = Objects.requireNonNull(random);
		}

		@Override
		protected String name() {
			return "random";
		}

		/**
		 * Returns a long in the range [0, n).
		 *
		 * @param n  the limit
		 * @return  a random long from the range [0, n)
		 */
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

	static class RandSeed extends AbstractLibFunction {

		/**
		 * The random number generator used by this function.
		 */
		protected final Random random;

		/**
		 * Constructs a new {@code math.randomseed} function that uses {@code random} as its
		 * random number generator.
		 *
		 * @param random  the random number generator to use, must not be {@code null}
		 *
		 * @throws NullPointerException  if {@code random} is {@code null}
		 */
		public RandSeed(Random random) {
			this.random = Objects.requireNonNull(random);
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

	static class Sin extends AbstractMathFunction1 {

		@Override
		protected String name() {
			return "sin";
		}

		@Override
		protected Number op(double x) {
			return Math.sin(x);
		}

	}

	static class Sqrt extends AbstractMathFunction1 {

		@Override
		protected String name() {
			return "sqrt";
		}

		@Override
		protected Number op(double x) {
			return Math.sqrt(x);
		}

	}

	static class Tan extends AbstractMathFunction1 {

		@Override
		protected String name() {
			return "tan";
		}

		@Override
		protected Number op(double x) {
			return Math.tan(x);
		}

	}

	static class ToInteger extends AbstractLibFunction {

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

	static class Type extends AbstractLibFunction {

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

	static class ULt extends AbstractLibFunction {

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
