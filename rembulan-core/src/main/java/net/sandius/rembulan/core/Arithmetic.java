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

import static net.sandius.rembulan.LuaMathOperators.rawadd;
import static net.sandius.rembulan.LuaMathOperators.rawdiv;
import static net.sandius.rembulan.LuaMathOperators.rawidiv;
import static net.sandius.rembulan.LuaMathOperators.rawmod;
import static net.sandius.rembulan.LuaMathOperators.rawmul;
import static net.sandius.rembulan.LuaMathOperators.rawpow;
import static net.sandius.rembulan.LuaMathOperators.rawsub;
import static net.sandius.rembulan.LuaMathOperators.rawunm;

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
 *   Arithmetic math = Arithmetic.of(a, b);
 *   if (math != null) {
 *       result = math.do_idiv(a, b);
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
	 * <p>Invokes arithmetic operations in {@link net.sandius.rembulan.LuaMathOperators}
	 * with all arguments converted to integers (i.e. {@code long}s)</p>
	 */
	public static final Arithmetic INTEGER = new IntegerArithmetic();

	/**
	 * Float arithmetic.
	 */
	public static final Arithmetic FLOAT = new FloatArithmetic();

	public abstract Number do_add(Number a, Number b);
	public abstract Number do_sub(Number a, Number b);
	public abstract Number do_mul(Number a, Number b);
	public abstract Double do_div(Number a, Number b);
	public abstract Number do_mod(Number a, Number b);
	public abstract Number do_idiv(Number a, Number b);
	public abstract Double do_pow(Number a, Number b);

	public abstract Number do_unm(Number n);

	private static final class IntegerArithmetic extends Arithmetic {

		private IntegerArithmetic() {
			// not to be instantiated by the outside world
		}

		@Override
		public Long do_add(Number a, Number b) {
			return rawadd(a.longValue(), b.longValue());
		}

		@Override
		public Long do_sub(Number a, Number b) {
			return rawsub(a.longValue(), b.longValue());
		}

		@Override
		public Long do_mul(Number a, Number b) {
			return rawmul(a.longValue(), b.longValue());
		}

		@Override
		public Double do_div(Number a, Number b) {
			return rawdiv(a.longValue(), b.longValue());
		}

		@Override
		public Long do_mod(Number a, Number b) {
			return rawmod(a.longValue(), b.longValue());
		}

		@Override
		public Long do_idiv(Number a, Number b) {
			return rawidiv(a.longValue(), b.longValue());
		}

		@Override
		public Double do_pow(Number a, Number b) {
			return rawpow(a.longValue(), b.longValue());
		}

		@Override
		public Long do_unm(Number n) {
			return rawunm(n.longValue());
		}

	}

	private static final class FloatArithmetic extends Arithmetic {

		private FloatArithmetic() {
			// not to be instantiated by the outside world
		}

		@Override
		public Double do_add(Number a, Number b) {
			return rawadd(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_sub(Number a, Number b) {
			return rawsub(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_mul(Number a, Number b) {
			return rawmul(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_div(Number a, Number b) {
			return rawdiv(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_mod(Number a, Number b) {
			return rawmod(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_idiv(Number a, Number b) {
			return rawidiv(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_pow(Number a, Number b) {
			return rawpow(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_unm(Number n) {
			return rawunm(n.doubleValue());
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
