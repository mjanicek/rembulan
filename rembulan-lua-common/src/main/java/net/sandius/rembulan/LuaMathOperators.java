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

package net.sandius.rembulan;

/**
 * A collection of static methods for performing the equivalents of Lua's arithmetic,
 * bitwise and numerical comparison operations.
 *
 * <p>This class and its methods exploits the isomorphism between Lua integers and
 * Java {@code long} on the one hand, and between Lua floats and Java {@code double}
 * on the other.</p>
 *
 * <p>For each operation, there are as many variants in the form of a static method
 * as there are valid type combinations. While all arithmetic operations
 * are defined in two variants (for two {@code long}s and two {@code double}s, e.g.
 * {@link #rawidiv(long, long)} and {@link #rawidiv(double, double)}), bitwise operations
 * have single variants (taking two {@code long}s, e.g. {@link #rawband(long, long)}),
 * and numerical comparison operations have four variants (for argument type combination).</p>
 *
 * <p>It is the task of a Lua implementation to select the appropriate method and
 * supply it with the required values; however, note that the method selection is well-behaved
 * under the type conversion rules of the Java programming language.</p>
 */
public final class LuaMathOperators {

	private LuaMathOperators() {
		// not to be instantiated or extended
	}

	// Arithmetic operators

	/**
	 * Returns the result of the addition of two {@code long}s, equivalent to the addition
	 * of two integers in Lua.
	 *
	 * @param a  first addend
	 * @param b  second addend
	 * @return  the value of the Lua expression {@code (a + b)},
	 *          where {@code a} and {@code b} are Lua integers
	 */
	public static long rawadd(long a, long b) {
		return a + b;
	}

	/**
	 * Returns the result of the addition of two {@code double}s, equivalent to the addition
	 * of two floats in Lua.
	 *
	 * @param a  first addend
	 * @param b  second addend
	 * @return  the value of the Lua expression {@code (a + b)},
	 *          where {@code a} and {@code b} are Lua floats
	 */
	public static double rawadd(double a, double b) {
		return a + b;
	}

	/**
	 * Returns the result of the subtraction of two {@code long}s, equivalent to the subtraction
	 * of two integers in Lua.
	 *
	 * @param a  the minuend
	 * @param b  the subtrahend
	 * @return  the value of the Lua expression {@code (a - b)},
	 *          where {@code a} and {@code b} are Lua integers
	 */
	public static long rawsub(long a, long b) {
		return a - b;
	}

	/**
	 * Returns the result of the subtraction of two {@code double}s, equivalent to the subtraction
	 * of two floats in Lua.
	 *
	 * @param a  the minuend
	 * @param b  the subtrahend
	 * @return  the value of the Lua expression {@code (a - b)},
	 *          where {@code a} and {@code b} are Lua floats
	 */
	public static double rawsub(double a, double b) {
		return a - b;
	}

	/**
	 * Returns the result of the multiplication of two {@code long}s, equivalent to
	 * the multiplication of two integers in Lua.
	 *
	 * @param a  first factor
	 * @param b  second factor
	 * @return  the value of the Lua expression {@code (a * b)},
	 *          where {@code a} and {@code b} are Lua integers
	 */
	public static long rawmul(long a, long b) {
		return a * b;
	}

	/**
	 * Returns the result of the multiplication of two {@code double}s, equivalent to
	 * the multiplication of two floats in Lua.
	 *
	 * @param a  first factor
	 * @param b  second factor
	 * @return  the value of the Lua expression {@code (a * b)},
	 *          where {@code a} and {@code b} are Lua floats
	 */
	public static double rawmul(double a, double b) {
		return a * b;
	}

	/**
	 * Returns the result of the division of two {@code long}s, equivalent to the float
	 * division of two integers in Lua. The result is always a {@code double}; when {@code b}
	 * is zero, the result is <i>NaN</i>.
	 *
	 * <p>Note that this behaviour differs from the standard Java integer division.</p>
	 *
	 * @param a  the dividend
	 * @param b  the divisor
	 * @return  the value of the Lua expression {@code (a / b)},
	 *          where {@code a} and {@code b} are Lua integers
	 */
	public static double rawdiv(long a, long b) {
		return ((double) a) / ((double) b);
	}

	/**
	 * Returns the result of the division of two {@code double}s, equivalent to the float
	 * division of two floats in Lua.
	 *
	 * @param a  the dividend
	 * @param b  the divisor
	 * @return  the value of the Lua expression {@code (a / b)},
	 *          where {@code a} and {@code b} are Lua floats
	 */
	public static double rawdiv(double a, double b) {
		return a / b;
	}

	/**
	 * Returns the floor modulus of two {@code long}s, equivalent to the modulus
	 * of two integers in Lua.
	 *
	 * <p>Note that in Lua,</p>
	 *
	 * <blockquote>
	 *     Modulo is defined as the remainder of a division that rounds the quotient
	 *     towards minus infinity (floor division).
	 * </blockquote>
	 *
	 * <p>This definition is <i>not</i> equivalent to the standard Java definition of modulo
	 * (which is the remainder of a division rounding toward <i>zero</i>).</p>
	 *
	 * @param a  the dividend
	 * @param b  the divisor
	 * @return  the value of the Lua expression {@code (a % b)},
	 *          where {@code a} and {@code b} are Lua integers
	 *
	 * @throws ArithmeticException  if {@code b} is zero
	 */
	public static long rawmod(long a, long b) {
		// Note: in JDK 8+, Math.floorMod could be used
		if (b == 0) throw new ArithmeticException("attempt to perform 'n%0'");
		else return a - b * (long) Math.floor((double) a / (double) b);
	}

	/**
	 * Returns the floor modulus of two {@code double}s, equivalent to the modulus
	 * of two floats in Lua.
	 *
	 * <p>Note that in Lua,</p>
	 *
	 * <blockquote>
	 *     Modulo is defined as the remainder of a division that rounds the quotient
	 *     towards minus infinity (floor division).
	 * </blockquote>
	 *
	 * <p>This definition is <i>not</i> equivalent to the standard Java definition of modulo
	 * (which is the remainder of a division rounding toward <i>zero</i>).</p>
	 *
	 * @param a  the dividend
	 * @param b  the divisor
	 * @return  the value of the Lua expression {@code (a % b)},
	 *          where {@code a} and {@code b} are Lua floats
	 */
	public static double rawmod(double a, double b) {
		return b != 0 ? a - b * Math.floor(a / b) : Double.NaN;
	}

	/**
	 * Returns the result of floor division of two {@code long}s, equivalent to the Lua
	 * floor division of two integers.
	 *
	 * <p>In Lua,</p>
	 *
	 * <blockquote>
	 *     Floor division (//) is a division that rounds the quotient towards minus infinity,
	 *     that is, the floor of the division of its operands.
	 * </blockquote>
	 *
	 * @param a  the dividend
	 * @param b  the divisor
	 * @return  the value of the Lua expression {@code (a // b)}
	 *          where {@code a} and {@code b} are Lua integers
	 *
	 * @throws ArithmeticException  if {@code b} is zero
	 */
	public static long rawidiv(long a, long b) {
		if (b == 0) throw new ArithmeticException("attempt to divide by zero");
		else {
			long q = a / b;
			return q * b == a || (a ^ b) >= 0 ? q : q - 1;
		}
	}

	/**
	 * Returns the result of floor division of two {@code double}s, equivalent to the Lua
	 * floor division of two floats:
	 *
	 * <p>In Lua,</p>
	 *
	 * <blockquote>
	 *     Floor division (//) is a division that rounds the quotient towards minus infinity,
	 *     that is, the floor of the division of its operands.
	 * </blockquote>
	 * 
	 * @param a  the dividend
	 * @param b  the divisor
	 * @return  the value of the Lua expression {@code (a // b)}
	 *          where {@code a} and {@code b} are Lua floats
	 */
	public static double rawidiv(double a, double b) {
		return Math.floor(a / b);
	}

	/**
	 * Returns the result of the exponentiation of two {@code long}s, equivalent to the Lua
	 * exponentiation of two integers. Note that the resulting value is a {@code double}.
	 *
	 * @param a  the base
	 * @param b  the exponent
	 * @return  the value of the Lua expression {@code (a ^ b)},
	 *          where {@code a} and {@code b} are Lua integers
	 */
	public static double rawpow(long a, long b) {
		return Math.pow((double) a, (double) b);
	}

	/**
	 * Returns the result of the exponentiation of two {@code double}s, equivalent to Lua
	 * exponentiation of two floats.
	 *
	 * @param a  the base
	 * @param b  the exponent
	 * @return  the value of the Lua expression {@code (a ^ b)},
	 *          where {@code a} and {@code b} are Lua floats
	 */
	public static double rawpow(double a, double b) {
		return Math.pow(a, b);
	}

	/**
	 * Returns the result of the (arithmetic) negation of a {@code long}, equivalent to
	 * the Lua unary minus on an integer.
	 *
	 * @param n  the operand
	 * @return  the value of the Lua expression {@code (-n)},
	 *          where {@code n} is a Lua integer
	 */
	public static long rawunm(long n) {
		return -n;
	}

	/**
	 * Returns the result of the (arithmetic) negation of a {@code long}, equivalent to
	 * the Lua unary minus on a float.
	 *
	 * @param n  the operand
	 * @return  the value of the Lua expression {@code (-n)},
	 *          where {@code n} is a Lua float
	 */
	public static double rawunm(double n) {
		return -n;
	}


	// Bitwise operators

	/**
	 * Returns the result of the bitwise AND of two {@code long}s, equivalent to the Lua
	 * bitwise AND of two integers.
	 *
	 * @param a  the first operand
	 * @param b  the second operand
	 * @return  the value of the Lua expression {@code (a & b)},
	 *          where {@code a} and {@code b} are Lua integers
	 */
	public static long rawband(long a, long b) {
		return a & b;
	}

	/**
	 * Returns the result of the bitwise OR of two {@code long}s, equivalent to the Lua
	 * bitwise OR of two integers.
	 *
	 * @param a  the first operand
	 * @param b  the second operand
	 * @return  the value of the Lua expression {@code (a | b)},
	 *          where {@code a} and {@code b} are Lua integers
	 */
	public static long rawbor(long a, long b) {
		return a | b;
	}

	/**
	 * Returns the result of the bitwise exclusive OR of two {@code long}s,
	 * equivalent to the Lua bitwise exclusive OR of two integers.
	 *
	 * @param a  the first operand
	 * @param b  the second operand
	 * @return  the value of the Lua expression {@code (a ~ b)},
	 *          where {@code a} and {@code b} are Lua integers
	 */
	public static long rawbxor(long a, long b) {
		return a ^ b;
	}

	/**
	 * Returns the result of the (bitwise) logical left shift, equivalent to the Lua bitwise
	 * left shift on two integers. Vacant bits are filled with zeros. When {@code b} is negative,
	 * the result is equal to the result of a right shift by {@code -b}.
	 *
	 * <p>Note that Lua's behaviour differs from Java's {@code <<} operator in that if
	 * {@code b} is greater than 64, the result is zero, as all bits have been shifted out.</p>
	 *
	 * @param a  the left-hand side operand
	 * @param b  the right-hand side operand (shift distance)
	 * @return  the value of the Lua expression {@code (a << b)},
	 *          where {@code a} and {@code b} are Lua integers
	 */
	public static long rawshl(long a, long b) {
		return b < 0 ? rawshr(a, -b) : (b < 64 ? a << b : 0);
	}

	/**
	 * Returns the result of the (bitwise) logical right shift, equivalent to the Lua bitwise
	 * right shift on two integers. Vacant bits are filled with zeros. When {@code b} is negative,
	 * the result is equal to the result of a left shift by {@code -b}.
	 *
	 * <p>Note that Lua's behaviour differs from Java's {@code >>>} operator in that if
	 * {@code b} is greater than 64, the result is zero, as all bits have been shifted out.</p>
	 *
	 * @param a  the left-hand side operand
	 * @param b  the right-hand side operand (shift distance)
	 * @return  the value of the Lua expression {@code (a << b)},
	 *          where {@code a} and {@code b} are Lua integers
	 */
	public static long rawshr(long a, long b) {
		return b < 0 ? rawshl(a, -b) : (b < 64 ? a >>> b : 0);
	}

	/**
	 * Returns the result of the bitwise unary NOT of a {@code long}, equivalent to the Lua
	 * bitwise unary NOT on an integer.
	 *
	 * @param n  the operand
	 * @return  the value of the Lua expression {@code (~b)},
	 *          where {@code n} is a Lua integer
	 */
	public static long rawbnot(long n) {
		return ~n;
	}


	// Numerical comparison operators

	private static final double MAX_LONG_AS_DOUBLE = (double) Long.MAX_VALUE;

	private static final double MIN_LONG_AS_DOUBLE = (double) Long.MIN_VALUE;

	/**
	 * Returns {@code true} iff the {@code double} {@code d} can be represented by
	 * a {@code long} without the loss of precision, i.e. if {@code ((long) d)}
	 * and {@code d} denote the same mathematical value.
	 *
	 * @param d  the {@code double} in question
	 * @return  {@code true} iff {@code d} can be represented by a {@code long}
	 *          without the loss of precision
	 */
	public static boolean hasExactIntegerRepresentation(double d) {
		long l = (long) d;
		return (double) l == d && l != Long.MAX_VALUE;
	}

	/**
	 * Returns {@code true} iff the {@code long} {@code l} can be represented by
	 * a {@code double} without the loss of precision, i.e. if {@code ((double) l)}
	 * and {@code l} denote the same mathematical value.
	 *
	 * @param l  the {@code long} in question
	 * @return  {@code true} iff {@code l} can be represented by a {@code double}
	 *          without the loss of precision
	 */
	public static boolean hasExactFloatRepresentation(long l) {
		double d = (double) l;
		return (long) d == l && l != Long.MAX_VALUE;
	}

	/**
	 * Returns {@code true} iff the {@code long}s {@code a} and {@code b} denote
	 * the same mathematical value. This is equivalent to the Lua numerical equality
	 * comparison of two integers.
	 *
	 * @param a  a {@code long}
	 * @param b  a {@code long} to be compared with {@code a} for mathematical equality
	 * @return  {@code true} iff the Lua expression {@code (a == b)},
	 *          where {@code a} and {@code b} are Lua integers,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean raweq(long a, long b) {
		return a == b;
	}

	/**
	 * Returns {@code true} iff the {@code long} {@code a} denotes the same mathematical
	 * value as the {@code double} {@code b}. This is equivalent to the Lua numerical
	 * equality comparison of an integer and a float.
	 *
	 * @param a  a {@code long}
	 * @param b  a {@code double} to be compared with {@code a} for mathematical equality
	 * @return  {@code true} iff the Lua expression {@code (a == b)},
	 *          where {@code a} is a Lua integer and {@code b} is a Lua float,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean raweq(long a, double b) {
		return hasExactFloatRepresentation(a) && (double) a == b;
	}

	/**
	 * Returns {@code true} iff the {@code double} {@code a} denotes the same mathematical
	 * value as the {@code long} {@code b}. This is equivalent to the Lua numerical equality
	 * comparison of a float and an integer.
	 *
	 * @param a  a {@code double}
	 * @param b  a {@code long} to be compared with {@code a} for mathematical equality
	 * @return  {@code true} iff the Lua expression {@code (a == b)},
	 *          where {@code a} is a Lua float and {@code b} is a Lua integer,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean raweq(double a, long b) {
		return hasExactFloatRepresentation(b) && a == (double) b;
	}

	/**
	 * Returns {@code true} iff the {@code double}s {@code a} and {@code b} denote
	 * the same mathematical value. This is equivalent to the Lua numerical equality
	 * comparison of two doubles.
	 *
	 * @param a  a {@code double}
	 * @param b  a {@code double} to be compared with {@code a} for mathematical equality
	 * @return  {@code true} iff the Lua expression {@code (a == b)},
	 *          where {@code a} and {@code b} are Lua floats,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean raweq(double a, double b) {
		return a == b;
	}

	/**
	 * Returns {@code true} iff the mathematical value of the {@code long} {@code a}
	 * is lesser than the mathematical value of the {@code long} {@code b}. This is equivalent
	 * to the Lua numerical lesser-than comparison of two integers.
	 * 
	 * @param a  a {@code long}
	 * @param b  a {@code long} to be compared with {@code a}
	 * @return  {@code true} iff the Lua expression {@code (a < b)},
	 *          where {@code a} and {@code b} are Lua integers,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean rawlt(long a, long b) {
		return a < b;
	}

	/**
	 * Returns {@code true} iff the mathematical value of the {@code long} {@code a}
	 * is lesser than the mathematical value of the {@code double} {@code b}. This
	 * is equivalent to the Lua numerical lesser-than comparison of an integer and a float.
	 *
	 * @param a  a {@code long}
	 * @param b  a {@code double} to be compared with {@code a}
	 * @return  {@code true} iff the Lua expression {@code (a < b)},
	 *          where {@code a} is a Lua integer and {@code b} is a Lua float,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean rawlt(long a, double b) {
		if (hasExactFloatRepresentation(a)) {
			return (double) a < b;
		}
		else {
			return b == b && b > MIN_LONG_AS_DOUBLE && (b >= MAX_LONG_AS_DOUBLE || a < (long) b);
		}
	}

	/**
	 * Returns {@code true} iff the mathematical value of the {@code double} {@code a}
	 * is lesser than the mathematical value of the {@code long} {@code b}. This
	 * is equivalent to the Lua numerical lesser-than comparison of a float and an integer.
	 *
	 * @param a  a {@code double}
	 * @param b  a {@code long} to be compared with {@code a}
	 * @return  {@code true} iff the Lua expression {@code (a < b)},
	 *          where {@code a} is a Lua float and {@code b} is a Lua integer,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean rawlt(double a, long b) {
		return a == a && !rawle(b, a);
	}

	/**
	 * Returns {@code true} iff the mathematical value of the {@code double} {@code a}
	 * is lesser than the mathematical value of the {@code double} {@code b}. This
	 * is equivalent to the Lua numerical lesser-than comparison of two floats.
	 *
	 * @param a  a {@code double}
	 * @param b  a {@code double} to be compared with {@code a}
	 * @return  {@code true} iff the Lua expression {@code (a < b)},
	 *          where {@code a} and {@code b} are Lua floats,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean rawlt(double a, double b) {
		return a < b;
	}

	/**
	 * Returns {@code true} iff the mathematical value of the {@code long} {@code a}
	 * is lesser than or equal to the mathematical value of the {@code long} {@code b}.
	 * This is equivalent to the Lua numerical lesser-than-or-equal comparison of
	 * two integers.
	 *
	 * @param a  a {@code long}
	 * @param b  a {@code long} to be compared with {@code a}
	 * @return  {@code true} iff the Lua expression {@code (a <= b)},
	 *          where {@code a} and {@code b} are Lua integers,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean rawle(long a, long b) {
		return a <= b;
	}

	/**
	 * Returns {@code true} iff the mathematical value of the {@code long} {@code a}
	 * is lesser than or equal to the mathematical value of the {@code double} {@code b}.
	 * This is equivalent to the Lua numerical lesser-than-or-equal comparison of
	 * an integer and a float.
	 *
	 * @param a  a {@code long}
	 * @param b  a {@code double} to be compared with {@code a}
	 * @return  {@code true} iff the Lua expression {@code (a <= b)},
	 *          where {@code a} is a Lua integer and {@code b} is a Lua float,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean rawle(long a, double b) {
		if (hasExactFloatRepresentation(a)) {
			return (double) a <= b;
		}
		else {
			return b == b && b > MIN_LONG_AS_DOUBLE && (b >= MAX_LONG_AS_DOUBLE || a <= (long) b);
		}
	}

	/**
	 * Returns {@code true} iff the mathematical value of the {@code double} {@code a}
	 * is lesser than or equal to the mathematical value of the {@code long} {@code b}.
	 * This is equivalent to the Lua numerical lesser-than-or-equal comparison of
	 * a float and an integer.
	 *
	 * @param a  a {@code double}
	 * @param b  a {@code long} to be compared with {@code a}
	 * @return  {@code true} iff the Lua expression {@code (a <= b)},
	 *          where {@code a} is a Lua float and {@code b} is a Lua integer,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean rawle(double a, long b) {
		return a == a && !rawlt(b, a);
	}

	/**
	 * Returns {@code true} iff the mathematical value of the {@code double} {@code a}
	 * is lesser than or equal to the mathematical value of the {@code double} {@code b}.
	 * This is equivalent to the Lua numerical lesser-than-or-equal comparison of
	 * two floats.
	 *
	 * @param a  a {@code double}
	 * @param b  a {@code double} to be compared with {@code a}
	 * @return  {@code true} iff the Lua expression {@code (a <= b)},
	 *          where {@code a} and {@code b} are Lua floats,
	 *          would evaluate to (Lua) <b>true</b>
	 */
	public static boolean rawle(double a, double b) {
		return a <= b;
	}

}
