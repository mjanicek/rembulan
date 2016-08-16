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

import net.sandius.rembulan.util.Check;

public abstract class RawOperators {

	private RawOperators() {
		// not to be instantiated
	}

	// Arithmetic operators

//	public static long rawadd(long a, long b) {
//		return a + b;
//	}
//
//	public static double rawadd(double a, double b) {
//		return a + b;
//	}
//
//	public static long rawsub(long a, long b) {
//		return a - b;
//	}
//
//	public static double rawsub(double a, double b) {
//		return a - b;
//	}
//
//	public static long rawmul(long a, long b) {
//		return a * b;
//	}
//
//	public static double rawmul(double a, double b) {
//		return a * b;
//	}

//	public static double rawdiv(long a, long b) {
//		return ((double) a) / ((double) b);
//	}
//
//	public static double rawdiv(double a, double b) {
//		return a / b;
//	}

	public static long rawmod(long a, long b) {
		if (b == 0) throw new ArithmeticException("attempt to perform 'n%0'");
		else return a - b * (long) Math.floor((double) a / (double) b);
	}

	public static double rawmod(double a, double b) {
		return b != 0 ? a - b * Math.floor(a / b) : Double.NaN;
	}

	public static double rawidiv(double a, double b) {
		return Math.floor(a / b);
	}

	public static long rawidiv(long a, long b) {
		if (b == 0) throw new ArithmeticException("attempt to divide by zero");
		else {
			long q = a / b;
			return q * b == a || (a ^ b) >= 0 ? q : q - 1;
		}
	}

	public static double rawpow(double a, double b) {
		return Math.pow(a, b);
	}

//	public static long rawunm(long n) {
//		return -n;
//	}
//
//	public static double rawunm(double n) {
//		return -n;
//	}

	// Bitwise operators

//	public static Number rawband(long la, long lb) {
//		return la & lb;
//	}
//
//	public static Number rawbor(long la, long lb) {
//		return la | lb;
//	}
//
//	public static Number rawbxor(long la, long lb) {
//		return la ^ lb;
//	}
//
//	public static Number rawbnot(long ln) {
//		return ~ln;
//	}

	public static long rawshl(long a, long b) {
		return b < 0 ? rawshr(a, -b) : (b < 64 ? a << b : 0);
	}

	public static long rawshr(long a, long b) {
		return b < 0 ? rawshl(a, -b) : (b < 64 ? a >>> b : 0);
	}

	// Comparison operators

	public static boolean raweq(Object a, Object b) {
		if (a == null && b == null) {
			// two nils
			return true;
		}
		else if (a == null) {
			// b is definitely not nil; also ensures that neither a nor b is null in the tests below
			return false;
		}
		else if (a instanceof Number && b instanceof Number) {
			Number na = (Number) a;
			Number nb = (Number) b;
			// must denote the same mathematical value
			return na.doubleValue() == nb.doubleValue() && na.longValue() == nb.longValue();
		}
		else if (a instanceof Boolean || a instanceof String || a instanceof Invokable) {
			// value-based equality
			return a.equals(b);
		}
		else {
			// reference-based equality
			return a == b;
		}
	}

	private static final double MAX_LONG_AS_DOUBLE = (double) Long.MAX_VALUE;

	private static final double MIN_LONG_AS_DOUBLE = (double) Long.MIN_VALUE;

	private static boolean hasExactFloatRepresentation(long l) {
		double d = (double) l;
		return (long) d == l && l != Long.MAX_VALUE;
	}

	public static boolean raweq(long a, long b) {
		return a == b;
	}

	public static boolean raweq(long a, double b) {
		return hasExactFloatRepresentation(a) && (double) a == b;
	}

	public static boolean raweq(double a, long b) {
		return hasExactFloatRepresentation(b) && a == (double) b;
	}

	public static boolean raweq(double a, double b) {
		return a == b;
	}

	public static boolean rawlt(long a, long b) {
		return a < b;
	}

	public static boolean rawlt(long a, double b) {
		if (hasExactFloatRepresentation(a)) {
			return (double) a < b;
		}
		else {
			return b == b && b > MIN_LONG_AS_DOUBLE && (b >= MAX_LONG_AS_DOUBLE || a < (long) b);
		}
	}

	public static boolean rawlt(double a, long b) {
		return a == a && !rawle(b, a);
	}

	public static boolean rawlt(double a, double b) {
		return a < b;
	}

	public static boolean rawle(long a, long b) {
		return a <= b;
	}

	public static boolean rawle(long a, double b) {
		if (hasExactFloatRepresentation(a)) {
			return (double) a <= b;
		}
		else {
			return b == b && b > MIN_LONG_AS_DOUBLE && (b >= MAX_LONG_AS_DOUBLE || a <= (long) b);
		}
	}

	public static boolean rawle(double a, long b) {
		return a == a && !rawlt(b, a);
	}

	public static boolean rawle(double a, double b) {
		return a <= b;
	}


	@Deprecated
	public static boolean rawlt(String a, String b) {
		return a.compareTo(b) < 0;
	}

	@Deprecated
	public static boolean rawle(String a, String b) {
		return a.compareTo(b) <= 0;
	}


	public static int stringLen(String s) {
		Check.notNull(s);
		return s.getBytes().length;  // FIXME: probably wasteful!
	}

}
