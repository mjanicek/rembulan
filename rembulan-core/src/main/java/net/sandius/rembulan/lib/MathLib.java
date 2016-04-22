package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;

/**
 * This library provides basic mathematical functions. It provides all its functions and constants
 * inside the table {@code math}. Functions with the annotation "integer/float" give integer
 * results for integer arguments and float results for float (or mixed) arguments. Rounding
 * functions ({@link #_ceil() <code>math.ceil</code>}, {@link #_floor() <code>math.floor</code>},
 * and {@link #_modf() <code>math.modf</code>}) return an integer when the result fits
 * in the range of an integer, or a float otherwise.
 */
public abstract class MathLib implements Lib {

	@Override
	public void installInto(LuaState state, Table env) {
		LibUtils.setIfNonNull(env, "abs", _abs());
		LibUtils.setIfNonNull(env, "acos", _acos());
		LibUtils.setIfNonNull(env, "asin", _asin());
		LibUtils.setIfNonNull(env, "atan", _atan());
		LibUtils.setIfNonNull(env, "ceil", _ceil());
		LibUtils.setIfNonNull(env, "cos", _cos());
		LibUtils.setIfNonNull(env, "deg", _deg());
		LibUtils.setIfNonNull(env, "exp", _exp());
		LibUtils.setIfNonNull(env, "floor", _floor());
		LibUtils.setIfNonNull(env, "fmod", _fmod());
		LibUtils.setIfNonNull(env, "huge", _huge());
		LibUtils.setIfNonNull(env, "log", _log());
		LibUtils.setIfNonNull(env, "max", _max());
		LibUtils.setIfNonNull(env, "maxinteger", _maxinteger());
		LibUtils.setIfNonNull(env, "min", _min());
		LibUtils.setIfNonNull(env, "mininteger", _mininteger());
		LibUtils.setIfNonNull(env, "modf", _modf());
		LibUtils.setIfNonNull(env, "pi", _pi());
		LibUtils.setIfNonNull(env, "rad", _rad());
		LibUtils.setIfNonNull(env, "random", _random());
		LibUtils.setIfNonNull(env, "randomseed", _randomseed());
		LibUtils.setIfNonNull(env, "sin", _sin());
		LibUtils.setIfNonNull(env, "sqrt", _sqrt());
		LibUtils.setIfNonNull(env, "tan", _tan());
		LibUtils.setIfNonNull(env, "tointeger", _tointeger());
		LibUtils.setIfNonNull(env, "type", _type());
		LibUtils.setIfNonNull(env, "ult", _ult());
	}

	/**
	 * {@code math.abs (x)}
	 *
	 * <p>Returns the absolute value of {@code x}. (integer/float)</p>
	 */
	public abstract Function _abs();

	/**
	 * {@code math.acos (x)}
	 *
	 * <p>Returns the arc cosine of {@code x} (in radians).</p>
	 */
	public abstract Function _acos();

	/**
	 * {@code math.asin (x)}
	 *
	 * <p>Returns the arc sine of {@code x} (in radians).</p>
	 */
	public abstract Function _asin();

	/**
	 * {@code math.atan (x)}
	 *
	 * <p>Returns the arc tangent of {@code y/x} (in radians), but uses the signs of both
	 * parameters to find the quadrant of the result. (It also handles correctly the case
	 * of {@code x} being zero.)</p>
	 *
	 * <p>The default value for {@code x} is 1, so that the call {@code math.atan(y)} returns
	 * the arc tangent of {@code y}.</p>
	 */
	public abstract Function _atan();

	/**
	 * {@code math.ceil (x)}
	 *
	 * <p>Returns the smallest integer larger than or equal to {@code x}.</p>
	 */
	public abstract Function _ceil();

	/**
	 * {@code math.cos (x)}
	 *
	 * <p>Returns the cosine of {@code x} (assumed to be in radians).</p>
	 */
	public abstract Function _cos();

	/**
	 * {@code math.deg (x)}
	 *
	 * <p>Returns the angle {@code x} (given in radians) in degrees.</p>
	 */
	public abstract Function _deg();

	/**
	 * {@code math.exp (x)}
	 *
	 * <p>Returns the value <i>e</i><sup>{@code x}</sup> (where <i>e</i> is the base
	 * of natural logarithms).</p>
	 */
	public abstract Function _exp();

	/**
	 * {@code math.floor (x)}
	 *
	 * <p>Returns the largest integral value smaller than or equal to {@code x}.</p>
	 */
	public abstract Function _floor();

	/**
	 * {@code math.fmod (x, y)}
	 *
	 * <p>Returns the remainder of the division of {@code x} by {@code y} that rounds
	 * the quotient towards zero. (integer/float)</p>
	 */
	public abstract Function _fmod();

	/**
	 * {@code math.huge}
	 *
	 * <p>The value {@code HUGE_VAL}, a value larger than or equal to any other numerical
	 * value.</p>
	 */
	public abstract Double _huge();

	/**
	 * {@code math.log (x [, base])}
	 *
	 * <p>Returns the logarithm of {@code x} in the given base. The default for {@code base}
	 * is <i>e</i> (so that the function returns the natural logarithm of {@code x}).</p>
	 */
	public abstract Function _log();

	/**
	 * {@code math.max (x, ···)}
	 *
	 * <p>Returns the argument with the maximum value, according to the Lua operator &lt;.
	 * (integer/float)</p>
	 */
	public abstract Function _max();

	/**
	 * An integer with the maximum value for an integer.
	 */
	public abstract Long _maxinteger();

	/**
	 * {@code math.min (x, ···)}
	 *
	 * <p>Returns the argument with the minimum value, according to the Lua operator &lt;.
	 * (integer/float)</p>
	 */
	public abstract Function _min();

	/**
	 * An integer with the minimum value for an integer.
	 */
	public abstract Long _mininteger();

	/**
	 * {@code math.modf (x)}
	 *
	 * <p>Returns the integral part of {@code x} and the fractional part of {@code x}.
	 * Its second result is always a float.</p>
	 */
	public abstract Function _modf();

	/**
	 * {@code math.pi}
	 *
	 * <p>The value of &pi;.</p>
	 */
	public abstract Double _pi();

	/**
	 * {@code math.rad (x)}
	 *
	 * <p>Returns the angle {@code x} (given in degrees) in radians.</p>
	 */
	public abstract Function _rad();

	/**
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
	 */
	public abstract Function _random();

	/**
	 * {@code math.randomseed (x)}
	 *
	 * <p>Sets {@code x} as the "seed" for the pseudo-random generator: equal seeds produce
	 * equal sequences of numbers.</p>
	 */
	public abstract Function _randomseed();

	/**
	 * {@code math.sin (x)}
	 *
	 * <p>Returns the sine of {@code x} (assumed to be in radians).</p>
	 */
	public abstract Function _sin();

	/**
	 * {@code math.sqrt (x)}
	 *
	 * <p>Returns the square root of {@code x}. (You can also use the expression {@code x^0.5}
	 * to compute this value.)</p>
	 */
	public abstract Function _sqrt();

	/**
	 * {@code math.tan (x)}
	 *
	 * <p>Returns the tangent of {@code x} (assumed to be in radians).</p>
	 */
	public abstract Function _tan();

	/**
	 * {@code math.tointeger (x)}
	 *
	 * <p>If the value {@code x} is convertible to an integer, returns that integer.
	 * Otherwise, returns <b>nil</b>.</p>
	*/
	public abstract Function _tointeger();

	/**
	 * {@code math.type (x)}
	 *
	 * <p>Returns {@code "integer"} if {@code x} is an integer, {@code "float"} if it is a float,
	 * or <b>nil</b> if {@code x} is not a number.</p>
	 */
	public abstract Function _type();

	/**
	 * {@code math.ult (m, n)}
	 *
	 * <p>Returns a boolean, <b>true</b> if integer {@code m} is below integer {@code n} when
	 * they are compared as unsigned integers.</p>
	 */
	public abstract Function _ult();

}
