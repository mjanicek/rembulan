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

package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaMathOperators;

/**
 * A representation of one of the two arithmetic modes (integer or float).
 *
 * <p>This class serves as a bridge between the representation of Lua numbers as
 * {@link java.lang.Number} and the appropriate dispatch of arithmetic operations
 * via the methods of {@link net.sandius.rembulan.LuaMathOperators}.</p>
 *
 * <p>There are two concrete instances of this class, {@link #FLOAT} (the float
 * arithmetic) and {@link #INTEGER} (the integer arithmetic). In order to obtain
 * the correct instance for the given numbers, use the static methods
 * {@link #of(Number, Number)} (for lookup based on two arguments, i.e. for binary
 * operations) or {@link #of(Number)} (for lookup based on a single argument, i.e.
 * for unary minus).</p>
 *
 * <p>The arithmetic methods of this class yield boxed results.</p>
 *
 * <p><b>Example:</b> Given two objects {@code a} and {@code b} of type {@code Number},
 *  use the {@code Arithmetic} class to compute the value of the Lua expression
 *  {@code (a // b)}:</p>
 *
 * <pre>
 *   // Number a, b
 *   final Number result;
 *   Arithmetic m = Arithmetic.of(a, b);
 *   if (m != null) {
 *       result = m.idiv(a, b);
 *   }
 *   else {
 *       throw new IllegalStateException("a or b is nil");
 *   }
 * </pre>
 */
public abstract class Arithmetic {

	private Arithmetic() {
		// not to be instantiated by the outside world
	}

	/**
	 * Integer arithmetic.
	 *
	 * <p>Invokes arithmetic operations from {@link LuaMathOperators} with all arguments
	 * converted to Lua integers (i.e. {@code long}s).</p>
	 */
	public static final Arithmetic INTEGER = new IntegerArithmetic();

	/**
	 * Float arithmetic.
	 *
	 * <p>Invokes arithmetic operations from {@link LuaMathOperators} with all arguments
	 * converted to Lua floats (i.e. {@code double}s).</p>
	 */
	public static final Arithmetic FLOAT = new FloatArithmetic();

	/**
	 * Returns the boxed result of the Lua addition of the two numbers
	 * {@code a} and {@code b}.
	 * 
	 * @param a  first addend, must not be {@code null}
	 * @param b  second addend, must not be {@code null}
	 * @return  the (boxed) value of the Lua expression {@code (a + b)}
	 * 
	 * @throws NullPointerException if {@code a} or {@code b} is {@code null}
	 */
	public abstract Number add(Number a, Number b);

	/**
	 * Returns the boxed result of the Lua subtraction of the two numbers
	 * {@code a} and {@code b}.
	 * 
	 * @param a  the minuend, must not be {@code null}
	 * @param b  the subtrahend, must not be {@code null}
	 * @return  the (boxed) value of the Lua expression {@code (a - b)}
	 * 
	 * @throws NullPointerException if {@code a} or {@code b} is {@code null}
	 */
	public abstract Number sub(Number a, Number b);

	/**
	 * Returns the boxed result of the Lua multiplication of the two numbers
	 * {@code a} and {@code b}.
	 * 
	 * @param a  first factor, must not be {@code null}
	 * @param b  second factor, must not be {@code null}
	 * @return  the (boxed) value of the Lua expression {@code (a * b)}
	 * 
	 * @throws NullPointerException if {@code a} or {@code b} is {@code null}
	 */
	public abstract Number mul(Number a, Number b);
	
	/**
	 * Returns the boxed result of the Lua float division of the two numbers
	 * {@code a} and {@code b}.
	 * 
	 * @param a  the dividend, must not be {@code null}
	 * @param b  the divisor, must not be {@code null}
	 * @return  the (boxed) value of the Lua expression {@code (a / b)}
	 * 
	 * @throws NullPointerException if {@code a} or {@code b} is {@code null}
	 */
	public abstract Double div(Number a, Number b);
	
	/**
	 * Returns the boxed result of the Lua modulo of the two numbers
	 * {@code a} and {@code b}.
	 * 
	 * @param a  the dividend, must not be {@code null}
	 * @param b  the divisor, must not be {@code null}
	 * @return  the (boxed) value of the Lua expression {@code (a % b)}
	 * 
	 * @throws NullPointerException if {@code a} or {@code b} is {@code null}
	 */
	public abstract Number mod(Number a, Number b);
	
	/**
	 * Returns the boxed result of the Lua floor division of the two numbers
	 * {@code a} and {@code b}.
	 * 
	 * @param a  the dividend, must not be {@code null}
	 * @param b  the divisor, must not be {@code null}
	 * @return  the (boxed) value of the Lua expression {@code (a // b)}
	 * 
	 * @throws NullPointerException if {@code a} or {@code b} is {@code null}
	 */
	public abstract Number idiv(Number a, Number b);
	
	/**
	 * Returns the boxed result of the Lua exponentiation of the two numbers
	 * {@code a} and {@code b}.
	 * 
	 * @param a  the base, must not be {@code null}
	 * @param b  the exponent, must not be {@code null}
	 * @return  the (boxed) value of the Lua expression {@code (a ^ b)}
	 * 
	 * @throws NullPointerException if {@code a} or {@code b} is {@code null}
	 */
	public abstract Double pow(Number a, Number b);
	
	/**
	 * Returns the boxed result of the Lua arithmetic negation of the number
	 * {@code n}.
	 * 
	 * @param n  the operand, must not be {@code null}
	 * @return  the (boxed) value of the Lua expression {@code (-n)}
	 * 
	 * @throws NullPointerException if {@code n} is {@code null}
	 */
	public abstract Number unm(Number n);

	private static final class IntegerArithmetic extends Arithmetic {

		@Override
		public Long add(Number a, Number b) {
			return LuaMathOperators.add(a.longValue(), b.longValue());
		}

		@Override
		public Long sub(Number a, Number b) {
			return LuaMathOperators.sub(a.longValue(), b.longValue());
		}

		@Override
		public Long mul(Number a, Number b) {
			return LuaMathOperators.mul(a.longValue(), b.longValue());
		}

		@Override
		public Double div(Number a, Number b) {
			return LuaMathOperators.div(a.longValue(), b.longValue());
		}

		@Override
		public Long mod(Number a, Number b) {
			return LuaMathOperators.mod(a.longValue(), b.longValue());
		}

		@Override
		public Long idiv(Number a, Number b) {
			return LuaMathOperators.idiv(a.longValue(), b.longValue());
		}

		@Override
		public Double pow(Number a, Number b) {
			return LuaMathOperators.pow(a.longValue(), b.longValue());
		}

		@Override
		public Long unm(Number n) {
			return LuaMathOperators.unm(n.longValue());
		}

	}

	private static final class FloatArithmetic extends Arithmetic {

		@Override
		public Double add(Number a, Number b) {
			return LuaMathOperators.add(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double sub(Number a, Number b) {
			return LuaMathOperators.sub(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double mul(Number a, Number b) {
			return LuaMathOperators.mul(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double div(Number a, Number b) {
			return LuaMathOperators.div(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double mod(Number a, Number b) {
			return LuaMathOperators.mod(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double idiv(Number a, Number b) {
			return LuaMathOperators.idiv(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double pow(Number a, Number b) {
			return LuaMathOperators.pow(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double unm(Number n) {
			return LuaMathOperators.unm(n.doubleValue());
		}

	}

	/**
	 * Return an arithmetic based on the concrete types of the arguments {@code a}
	 * and {@code b}.
	 *
	 * <p>If either of {@code a} or {@code b} is {@code null}, returns {@code null}.
	 * Otherwise, if either of {@code a} or {@code b} a Lua float, returns {@link #FLOAT}.
	 * If {@code a} and {@code b} are both Lua integers, returns {@link #INTEGER}.</p>
	 *
	 * @param a  first argument, may be {@code null}
	 * @param b  second argument, may be {@code null}
	 * @return  {@code null} if {@code a} or {@code b} is {@code null};
	 *          {@link #FLOAT} if {@code a} or {@code b} is a Lua float;
	 *          {@link #INTEGER} if {@code a} and {@code b} are Lua integers
	 */
	public static Arithmetic of(Number a, Number b) {
		if (a == null || b == null) {
			return null;
		}
		else if ((a instanceof Double || a instanceof Float)
				|| (b instanceof Double || b instanceof Float)) {
			return FLOAT;
		}
		else {
			return INTEGER;
		}
	}

	/**
	 * Return an arithmetic based on the concrete type of the argument {@code n}.
	 *
	 * If {@code n} is {@code null}, returns {@code null}; otherwise, returns
	 * {@link #FLOAT} if {@code n} is a Lua float, or {@link #INTEGER} if {@code n}
	 * is a Lua integer.
	 *
	 * @param n  the argument, may be {@code null}
	 * @return  {@code null} if {@code n} is {@code null};
	 *          {@link #FLOAT} if {@code n} is a Lua float;
	 *          {@link #INTEGER} if {@code n} is a Lua integer
	 */
	public static Arithmetic of(Number n) {
		if (n == null) {
			return null;
		}
		else if (n instanceof Double || n instanceof Float) {
			return FLOAT;
		}
		else {
			return INTEGER;
		}
	}

}
