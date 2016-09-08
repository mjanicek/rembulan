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

package net.sandius.rembulan.runtime;

import net.sandius.rembulan.Arithmetic;
import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.LuaMathOperators;
import net.sandius.rembulan.MetatableProvider;
import net.sandius.rembulan.Metatables;
import net.sandius.rembulan.Ordering;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Userdata;

import java.util.Objects;

/**
 * A static class for dispatching operations according to the semantics of Lua 5.3.
 */
public final class Dispatch {

	private Dispatch() {
		// not to be instantiated
	}

	static LuaFunction callTarget(MetatableProvider metatableProvider, Object target) {
		if (target instanceof LuaFunction) {
			return (LuaFunction) target;
		}
		else {
			Object handler = Metatables.getMetamethod(metatableProvider, Metatables.MT_CALL, target);

			if (handler instanceof LuaFunction) {
				return (LuaFunction) handler;
			}
			else {
				throw IllegalOperationAttemptException.call(target);
			}
		}
	}

	static void mt_invoke(ExecutionContext context, Object target) throws ResolvedControlThrowable {
		LuaFunction fn = callTarget(context, target);
		if (fn == target) fn.invoke(context);
		else fn.invoke(context, target);
	}

	static void mt_invoke(ExecutionContext context, Object target, Object arg1) throws ResolvedControlThrowable {
		LuaFunction fn = callTarget(context, target);
		if (fn == target) fn.invoke(context, arg1);
		else fn.invoke(context, target, arg1);
	}

	static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2) throws ResolvedControlThrowable {
		LuaFunction fn = callTarget(context, target);
		if (fn == target) fn.invoke(context, arg1, arg2);
		else fn.invoke(context, target, arg1, arg2);
	}

	static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3) throws ResolvedControlThrowable {
		LuaFunction fn = callTarget(context, target);
		if (fn == target) fn.invoke(context, arg1, arg2, arg3);
		else fn.invoke(context, target, arg1, arg2, arg3);
	}

	static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4) throws ResolvedControlThrowable {
		LuaFunction fn = callTarget(context, target);
		if (fn == target) fn.invoke(context, arg1, arg2, arg3, arg4);
		else fn.invoke(context, target, arg1, arg2, arg3, arg4);
	}

	static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ResolvedControlThrowable {
		LuaFunction fn = callTarget(context, target);
		if (fn == target) fn.invoke(context, arg1, arg2, arg3, arg4, arg5);
		else fn.invoke(context, new Object[] { target, arg1, arg2, arg3, arg4, arg5 });
	}

	static void mt_invoke(ExecutionContext context, Object target, Object[] args) throws ResolvedControlThrowable {
		LuaFunction fn = callTarget(context, target);
		if (fn == target) {
			fn.invoke(context, args);
		}
		else {
			Object[] mtArgs = new Object[args.length + 1];
			mtArgs[0] = target;
			System.arraycopy(args, 0, mtArgs, 1, args.length);
			fn.invoke(context, mtArgs);
		}
	}

	/**
	 * Evaluates tail calls stored in the return buffer associated with the execution
	 * context {@code context}.
	 * <b>This method throws a {@link ResolvedControlThrowable}</b>:
	 * this method is expected to have resolved non-local control changes up to the point
	 * of its invocation.
	 *
	 * @param context  execution context, must not be {@code null}
	 *
	 * @throws ResolvedControlThrowable  if a tail call initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	static void evaluateTailCalls(ExecutionContext context) throws ResolvedControlThrowable {
		ReturnBuffer r = context.getReturnBuffer();
		while (r.isCall()) {
			Object target = r.getCallTarget();
			switch (r.size()) {
				case 0: mt_invoke(context, target); break;
				case 1: mt_invoke(context, target, r.get0()); break;
				case 2: mt_invoke(context, target, r.get0(), r.get1()); break;
				case 3: mt_invoke(context, target, r.get0(), r.get1(), r.get2()); break;
				case 4: mt_invoke(context, target, r.get0(), r.get1(), r.get2(), r.get3()); break;
				case 5: mt_invoke(context, target, r.get0(), r.get1(), r.get2(), r.get3(), r.get4()); break;
				default: mt_invoke(context, target, r.getAsArray()); break;
			}
		}
	}

	/**
	 * Calls the object {@code target} with no arguments.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * <p>This is the equivalent of the Lua expression</p>
	 * <pre>
	 *     target()
	 * </pre>
	 * <p>including metamethod handling and tail call evaluation. Consequently, {@code target}
	 * may be any value (i.e., is not required to be a function).</p>
	 *
	 * <p>The results of the call will be stored in the return buffer associated with
	 * {@code context}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param target  call target, may be {@code null}
	 *
	 * @throws UnresolvedControlThrowable  if the call initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void call(ExecutionContext context, Object target)
			throws UnresolvedControlThrowable {
		try {
			mt_invoke(context, target);
			evaluateTailCalls(context);
		}
		catch (ResolvedControlThrowable ct) {
			throw ct.unresolve();
		}
	}

	/**
	 * Calls the object {@code target} with a single argument {@code arg1}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * <p>This is the equivalent of the Lua expression</p>
	 * <pre>
	 *     target(arg1)
	 * </pre>
	 * <p>including metamethod handling and tail call evaluation. Consequently, {@code target}
	 * may be any value (i.e., is not required to be a function).</p>
	 *
	 * <p>The results of the call will be stored in the return buffer associated with
	 * {@code context}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param target  call target, may be {@code null}
	 * @param arg1  the first argument to the call, may be {@code null}
	 *
	 * @throws UnresolvedControlThrowable  if the call initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void call(ExecutionContext context, Object target, Object arg1)
			throws UnresolvedControlThrowable {
		try {
			mt_invoke(context, target, arg1);
			evaluateTailCalls(context);
		}
		catch (ResolvedControlThrowable ct) {
			throw ct.unresolve();
		}
	}

	/**
	 * Calls the object {@code target} with the arguments {@code arg1} and {@code arg2}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * <p>This is the equivalent of the Lua expression</p>
	 * <pre>
	 *     target(arg1, arg2)
	 * </pre>
	 * <p>including metamethod handling and tail call evaluation. Consequently, {@code target}
	 * may be any value (i.e., is not required to be a function).</p>
	 *
	 * <p>The results of the call will be stored in the return buffer associated with
	 * {@code context}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param target  call target, may be {@code null}
	 * @param arg1  the first argument to the call, may be {@code null}
	 * @param arg2  the second argument to the call, may be {@code null}
	 *
	 * @throws UnresolvedControlThrowable  if the call initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2)
			throws UnresolvedControlThrowable {
		try {
			mt_invoke(context, target, arg1, arg2);
			evaluateTailCalls(context);
		}
		catch (ResolvedControlThrowable ct) {
			throw ct.unresolve();
		}
	}

	/**
	 * Calls the object {@code target} with the arguments {@code arg1}, {@code arg2} and
	 * {@code arg3}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * <p>This is the equivalent of the Lua expression</p>
	 * <pre>
	 *     target(arg1, arg2, arg3)
	 * </pre>
	 * <p>including metamethod handling and tail call evaluation. Consequently, {@code target}
	 * may be any value (i.e., is not required to be a function).</p>
	 *
	 * <p>The results of the call will be stored in the return buffer associated with
	 * {@code context}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param target  call target, may be {@code null}
	 * @param arg1  the first argument to the call, may be {@code null}
	 * @param arg2  the second argument to the call, may be {@code null}
	 * @param arg3  the third argument to the call, may be {@code null}
	 *
	 * @throws UnresolvedControlThrowable  if the call initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3)
			throws UnresolvedControlThrowable {
		try {
			mt_invoke(context, target, arg1, arg2, arg3);
			evaluateTailCalls(context);
		}
		catch (ResolvedControlThrowable ct) {
			throw ct.unresolve();
		}
	}

	/**
	 * Calls the object {@code target} with the arguments {@code arg1}, {@code arg2},
	 * {@code arg3} and {@code arg4}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * <p>This is the equivalent of the Lua expression</p>
	 * <pre>
	 *     target(arg1, arg2, arg3, arg4)
	 * </pre>
	 * <p>including metamethod handling and tail call evaluation. Consequently, {@code target}
	 * may be any value (i.e., is not required to be a function).</p>
	 *
	 * <p>The results of the call will be stored in the return buffer associated with
	 * {@code context}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param target  call target, may be {@code null}
	 * @param arg1  the first argument to the call, may be {@code null}
	 * @param arg2  the second argument to the call, may be {@code null}
	 * @param arg3  the third argument to the call, may be {@code null}
	 * @param arg4  the fourth argument to the call, may be {@code null}
	 *
	 * @throws UnresolvedControlThrowable  if the call initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4)
			throws UnresolvedControlThrowable {
		try {
			mt_invoke(context, target, arg1, arg2, arg3, arg4);
			evaluateTailCalls(context);
		}
		catch (ResolvedControlThrowable ct) {
			throw ct.unresolve();
		}
	}

	/**
	 * Calls the object {@code target} with the arguments {@code arg1}, {@code arg2},
	 * {@code arg3}, {@code arg4} and {@code arg5}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * <p>This is the equivalent of the Lua expression</p>
	 * <pre>
	 *     target(arg1, arg2, arg3, arg4, arg5)
	 * </pre>
	 * <p>including metamethod handling and tail call evaluation. Consequently, {@code target}
	 * may be any value (i.e., is not required to be a function).</p>
	 *
	 * <p>The results of the call will be stored in the return buffer associated with
	 * {@code context}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param target  call target, may be {@code null}
	 * @param arg1  the first argument to the call, may be {@code null}
	 * @param arg2  the second argument to the call, may be {@code null}
	 * @param arg3  the third argument to the call, may be {@code null}
	 * @param arg4  the fourth argument to the call, may be {@code null}
	 * @param arg5  the fifth argument to the call, may be {@code null}
	 *
	 * @throws UnresolvedControlThrowable  if the call initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5)
			throws UnresolvedControlThrowable {
		try {
			mt_invoke(context, target, arg1, arg2, arg3, arg4, arg5);
			evaluateTailCalls(context);
		}
		catch (ResolvedControlThrowable ct) {
			throw ct.unresolve();
		}
	}

	/**
	 * Calls the object {@code target} with the arguments passed in the array {@code args}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * <p>This is the equivalent of the Lua expression</p>
	 * <pre>
	 *     target(a_0, ..., a_n)
	 * </pre>
	 * <p>where {@code a_i} denotes the value of {@code args[i]} and {@code n} is equal
	 * to {@code (args.length - 1)}, including metamethod handling and tail call evaluation.
	 * Consequently, {@code target} may be any value (i.e., is not required to be a function).</p>
	 *
	 * <p>Following the contract of {@link LuaFunction#invoke(ExecutionContext, Object[])},
	 * the array {@code args} passed to the called function may not be modified by
	 * the function and the reference to {@code args} must not be retained by the function beyond
	 * the scope of this method's invocation. It is therefore safe to assume that the contents of
	 * {@code args} will be the same after this method has returned. (No need for a defensive
	 * copy.)</p>
	 *
	 * <p>The results of the call will be stored in the return buffer associated with
	 * {@code context}.</p>
	 *
	 * <p>If {@code args} is {@code null}, then the behaviour of this method is undefined.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param target  call target, may be {@code null}
	 * @param args  call arguments, must not be {@code null}
	 *
	 * @throws UnresolvedControlThrowable  if the call initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void call(ExecutionContext context, Object target, Object[] args)
			throws UnresolvedControlThrowable {
		try {
			mt_invoke(context, target, args);
			evaluateTailCalls(context);
		}
		catch (ResolvedControlThrowable ct) {
			throw ct.unresolve();
		}
	}

	private static void try_mt_arithmetic(ExecutionContext context, String event, Object a, Object b) throws UnresolvedControlThrowable {
		Object handler = Metatables.binaryHandlerFor(context, event, a, b);

		if (handler != null) {
			call(context, handler, a, b);
		}
		else {
			throw IllegalOperationAttemptException.arithmetic(a, b);
		}
	}

	private static void try_mt_arithmetic(ExecutionContext context, String event, Object o) throws UnresolvedControlThrowable {
		Object handler = Metatables.getMetamethod(context, event, o);

		if (handler != null) {
			call(context, handler, o);
		}
		else {
			throw IllegalOperationAttemptException.arithmetic(o);
		}
	}

	/**
	 * Evaluates the Lua expression {@code a + b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void add(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic math = Arithmetic.of(na, nb);

		if (math != null) {
			context.getReturnBuffer().setTo(math.add(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_ADD, a, b);
		}
	}

	/**
	 * Returns the value of the Lua expression {@code a + b}, where {@code a} and {@code b}
	 * are numbers.
	 *
	 * <p>Note that when {@code a} and {@code b} are numbers, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a + b}
	 *
	 * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static Number add(Number a, Number b) {
		return Arithmetic.of(a, b).add(a, b);
	}

	/**
	 * Evaluates the Lua expression {@code a - b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void sub(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnBuffer().setTo(m.sub(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_SUB, a, b);
		}
	}

	/**
	 * Returns the value of the Lua expression {@code a - b}, where {@code a} and {@code b}
	 * are numbers.
	 *
	 * <p>Note that when {@code a} and {@code b} are numbers, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a - b}
	 *
	 * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static Number sub(Number a, Number b) {
		return Arithmetic.of(a, b).sub(a, b);
	}

	/**
	 * Evaluates the Lua expression {@code a * b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void mul(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnBuffer().setTo(m.mul(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_MUL, a, b);
		}
	}

	/**
	 * Returns the value of the Lua expression {@code a * b}, where {@code a} and {@code b}
	 * are numbers.
	 *
	 * <p>Note that when {@code a} and {@code b} are numbers, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a * b}
	 *
	 * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static Number mul(Number a, Number b) {
		return Arithmetic.of(a, b).mul(a, b);
	}

	/**
	 * Evaluates the Lua expression {@code a / b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void div(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnBuffer().setTo(m.div(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_DIV, a, b);
		}
	}

	/**
	 * Returns the value of the Lua expression {@code a / b}, where {@code a} and {@code b}
	 * are numbers.
	 *
	 * <p>Note that when {@code a} and {@code b} are numbers, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a / b}
	 *
	 * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static Number div(Number a, Number b) {
		return Arithmetic.of(a, b).div(a, b);
	}

	/**
	 * Evaluates the Lua expression {@code a % b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void mod(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnBuffer().setTo(m.mod(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_MOD, a, b);
		}
	}

	/**
	 * Returns the value of the Lua expression {@code a % b}, where {@code a} and {@code b}
	 * are numbers.
	 *
	 * <p>Note that when {@code a} and {@code b} are numbers, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a % b}
	 *
	 * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static Number mod(Number a, Number b) {
		return Arithmetic.of(a, b).mod(a, b);
	}

	/**
	 * Evaluates the Lua expression {@code a // b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void idiv(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnBuffer().setTo(m.idiv(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_IDIV, a, b);
		}
	}

	/**
	 * Returns the value of the Lua expression {@code a // b}, where {@code a} and {@code b}
	 * are numbers.
	 *
	 * <p>Note that when {@code a} and {@code b} are numbers, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a // b}
	 *
	 * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static Number idiv(Number a, Number b) {
		return Arithmetic.of(a, b).idiv(a, b);
	}

	/**
	 * Evaluates the Lua expression {@code a ^ b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void pow(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnBuffer().setTo(m.pow(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_POW, a, b);
		}
	}

	/**
	 * Returns the value of the Lua expression {@code a ^ b}, where {@code a} and {@code b}
	 * are numbers.
	 *
	 * <p>Note that when {@code a} and {@code b} are numbers, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a ^ b}
	 *
	 * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static Number pow(Number a, Number b) {
		return Arithmetic.of(a, b).pow(a, b);
	}

	private static void try_mt_bitwise(ExecutionContext context, String event, Object a, Object b) throws UnresolvedControlThrowable {
		Object handler = Metatables.binaryHandlerFor(context, event, a, b);

		if (handler != null) {
			call(context, handler, a, b);
		}
		else {
			throw IllegalOperationAttemptException.bitwise(a, b);
		}
	}

	private static void try_mt_bitwise(ExecutionContext context, String event, Object o) throws UnresolvedControlThrowable {
		Object handler = Metatables.getMetamethod(context, event, o);

		if (handler != null) {
			call(context, handler, o);
		}
		else {
			throw IllegalOperationAttemptException.bitwise(o);
		}
	}

	/**
	 * Evaluates the Lua expression {@code a & b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void band(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Long la = Conversions.integerValueOf(a);
		Long lb = Conversions.integerValueOf(b);

		if (la != null && lb != null) {
			context.getReturnBuffer().setTo(LuaMathOperators.band(la, lb));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BAND, a, b);
		}
	}

	/**
	 * Evaluates the Lua expression {@code a | b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void bor(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Long la = Conversions.integerValueOf(a);
		Long lb = Conversions.integerValueOf(b);

		if (la != null && lb != null) {
			context.getReturnBuffer().setTo(LuaMathOperators.bor(la, lb));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BOR, a, b);
		}
	}

	/**
	 * Evaluates the Lua expression {@code a ~ b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void bxor(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Long la = Conversions.integerValueOf(a);
		Long lb = Conversions.integerValueOf(b);

		if (la != null && lb != null) {
			context.getReturnBuffer().setTo(LuaMathOperators.bxor(la, lb));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BXOR, a, b);
		}
	}

	/**
	 * Evaluates the Lua expression {@code a << b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void shl(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Long la = Conversions.integerValueOf(a);
		Long lb = Conversions.integerValueOf(b);

		if (la != null && lb != null) {
			context.getReturnBuffer().setTo(LuaMathOperators.shl(la, lb));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_SHL, a, b);
		}
	}

	/**
	 * Evaluates the Lua expression {@code a >> b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void shr(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Long la = Conversions.integerValueOf(a);
		Long lb = Conversions.integerValueOf(b);

		if (la != null && lb != null) {
			context.getReturnBuffer().setTo(LuaMathOperators.shr(la, lb));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_SHR, a, b);
		}
	}

	/**
	 * Evaluates the Lua expression {@code -a}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param o  the argument, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void unm(ExecutionContext context, Object o) throws UnresolvedControlThrowable {
		Number no = Conversions.arithmeticValueOf(o);
		Arithmetic m = Arithmetic.of(no);
		if (m != null) {
			context.getReturnBuffer().setTo(m.unm(no));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_UNM, o);
		}
	}

	/**
	 * Returns the value of the Lua expression {@code -n}, where {@code n} is a number.
	 *
	 * <p>Note that when {@code n} is a number, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param n  the argument, must not be {@code null}
	 * @return  the value of the Lua expression {@code -a}
	 *
	 * @throws NullPointerException  if {@code n} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static Number unm(Number n) {
		return Arithmetic.of(n).unm(n);
	}

	/**
	 * Evaluates the Lua expression {@code ~o}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param o  the argument, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void bnot(ExecutionContext context, Object o) throws UnresolvedControlThrowable {
		Long lo = Conversions.integerValueOf(o);

		if (lo != null) {
			context.getReturnBuffer().setTo(LuaMathOperators.bnot(lo));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BNOT, o);
		}
	}

	/**
	 * Returns the value of the Lua expression {@code #s}, where {@code s} is a string.
	 *
	 * <p>Note that when {@code s} is a string, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param s  the string argument, must not be {@code null}
	 * @return  the value of the Lua expression {@code #s}
	 *
	 * @throws NullPointerException  if {@code s} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static long len(String s) {
		return s.getBytes().length;  // FIXME: wasteful!
	}

	/**
	 * Evaluates the Lua expression {@code #o}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param o  the argument, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void len(ExecutionContext context, Object o) throws UnresolvedControlThrowable {
		if (o instanceof String) {
			context.getReturnBuffer().setTo(len((String) o));
		}
		else {
			Object handler = Metatables.getMetamethod(context, Metatables.MT_LEN, o);
			if (handler != null) {
				call(context, handler, o);
			}
			else if (o instanceof Table) {
				context.getReturnBuffer().setTo((long) ((Table) o).rawlen());
			}
			else {
				throw IllegalOperationAttemptException.length(o);
			}
		}
	}

	/**
	 * Evaluates the Lua expression {@code a .. b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void concat(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		String sa = Conversions.stringValueOf(a);
		String sb = Conversions.stringValueOf(b);

		if (sa != null && sb != null) {
			context.getReturnBuffer().setTo(sa.concat(sb));
		}
		else {
			Object handler = Metatables.binaryHandlerFor(context, Metatables.MT_CONCAT, a, b);
			if (handler != null) {
				call(context, handler, a, b);
			}
			else {
				throw IllegalOperationAttemptException.concatenate(a, b);
			}
		}
	}

	private static final CmpResultResumable CMP_RESULT_RESUMABLE_TRUE = new CmpResultResumable(true);
	private static final CmpResultResumable CMP_RESULT_RESUMABLE_FALSE = new CmpResultResumable(false);

	private static Resumable cmpResultResumable(boolean cmpTo) {
		return cmpTo
				? CMP_RESULT_RESUMABLE_TRUE
				: CMP_RESULT_RESUMABLE_FALSE;
	}

	private static class CmpResultResumable implements Resumable {

		private final boolean cmpTo;

		public CmpResultResumable(boolean cmpTo) {
			this.cmpTo = cmpTo;
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			ReturnBuffer result = context.getReturnBuffer();
			boolean resultValue = Conversions.booleanValueOf(result.get0());
			result.setTo(cmpTo == resultValue);
		}

	}

	private static void _call_comparison_mt(ExecutionContext context, boolean cmpTo, Object handler, Object a, Object b) throws UnresolvedControlThrowable {
		try {
			call(context, handler, a, b);
		}
		catch (UnresolvedControlThrowable ct) {
			// suspended in the metamethod call
			throw ct.resolve(cmpResultResumable(cmpTo), null).unresolve();
		}
		// not suspended: set the result, possibly flipping it
		ReturnBuffer result = context.getReturnBuffer();
		result.setTo(cmpTo == Conversions.booleanValueOf(result.get0()));
	}

	private static void eq(ExecutionContext context, boolean polarity, Object a, Object b) throws UnresolvedControlThrowable {
		boolean rawEqual = Ordering.isRawEqual(a, b);

		if (!rawEqual
				&& ((a instanceof Table && b instanceof Table)
				|| (a instanceof Userdata && b instanceof Userdata))) {

			Object handler = Metatables.binaryHandlerFor(context, Metatables.MT_EQ, a, b);

			if (handler != null) {
				_call_comparison_mt(context, polarity, handler, a, b);
				return;
			}

			// else keep the result as false
		}

		context.getReturnBuffer().setTo(rawEqual == polarity);
	}

	/**
	 * Evaluates the Lua expression {@code a == b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void eq(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		eq(context, true, a, b);
	}

	/**
	 * Evaluates the Lua expression {@code a != b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void neq(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		eq(context, false, a, b);
	}

	/**
	 * Returns the value of the Lua expression {@code a == b}, where {@code a} and {@code b}
	 * are numbers.
	 *
	 * <p>Note that when {@code a} and {@code b} are numbers, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a == b}
	 *
	 * @throws NullPointerException  if {@code a} of {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static boolean eq(Number a, Number b) {
		return Ordering.NUMERIC.eq(a, b);
	}

	/**
	 * Evaluates the Lua expression {@code a < b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void lt(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Ordering c = Ordering.of(a, b);
		if (c != null) {
			@SuppressWarnings("unchecked")
			boolean result = c.lt(a, b);
			context.getReturnBuffer().setTo(result);
		}
		else {
			Object handler = Metatables.binaryHandlerFor(context, Metatables.MT_LT, a, b);

			if (handler != null) {
				_call_comparison_mt(context, true, handler, a, b);
			}
			else {
				throw IllegalOperationAttemptException.comparison(a, b);
			}
		}
	}

	/**
	 * Returns the value of the Lua expression {@code a < b}, where {@code a} and {@code b}
	 * are numbers.
	 *
	 * <p>Note that when {@code a} and {@code b} are numbers, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a < b}
	 *
	 * @throws NullPointerException  if {@code a} of {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static boolean lt(Number a, Number b) {
		return Ordering.NUMERIC.lt(a, b);
	}

	/**
	 * Returns the value of the Lua expression {@code a < b}, where {@code a} and {@code b}
	 * are strings.
	 *
	 * <p>Note that when {@code a} and {@code b} are strings, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a < b}
	 *
	 * @throws NullPointerException  if {@code a} of {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static boolean lt(String a, String b) {
		return Ordering.STRING.lt(a, b);
	}

	/**
	 * Evaluates the Lua expression {@code a <= b}, including the handling of metamethods,
	 * and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param a  the first operand, may be any value
	 * @param b  the second operand, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void le(ExecutionContext context, Object a, Object b) throws UnresolvedControlThrowable {
		Ordering c = Ordering.of(a, b);
		if (c != null) {
			@SuppressWarnings("unchecked")
			boolean result = c.le(a, b);
			context.getReturnBuffer().setTo(result);
		}
		else {
			Object le_handler = Metatables.binaryHandlerFor(context, Metatables.MT_LE, a, b);

			if (le_handler != null) {
				_call_comparison_mt(context, true, le_handler, a, b);
			}
			else {
				// TODO: verify that (a, b) is the order in which the metamethod is looked up
				Object lt_handler = Metatables.binaryHandlerFor(context, Metatables.MT_LT, a, b);

				if (lt_handler != null) {
					// will be evaluating "not (b < a)"
					_call_comparison_mt(context, false, lt_handler, b, a);
				}
				else {
					throw IllegalOperationAttemptException.comparison(a, b);
				}
			}
		}
	}

	/**
	 * Returns the value of the Lua expression {@code a <= b}, where {@code a} and {@code b}
	 * are numbers.
	 *
	 * <p>Note that when {@code a} and {@code b} are numbers, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a <= b}
	 *
	 * @throws NullPointerException  if {@code a} of {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static boolean le(Number a, Number b) {
		return Ordering.NUMERIC.le(a, b);
	}

	/**
	 * Returns the value of the Lua expression {@code a <= b}, where {@code a} and {@code b}
	 * are strings.
	 *
	 * <p>Note that when {@code a} and {@code b} are strings, no metamethods will be consulted,
	 * and that consequently, this method does not throw {@code UnresolvedControlThrowable}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @return  the value of the Lua expression {@code a <= b}
	 *
	 * @throws NullPointerException  if {@code a} of {@code b} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static boolean le(String a, String b) {
		return Ordering.STRING.le(a, b);
	}

	/**
	 * Evaluates the Lua expression {@code table[key]} (in non-assignment context) including
	 * the handling of metamethods, and stores the result to the return buffer associated with
	 * {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param table  the target, may be any value
	 * @param key  the key, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void index(ExecutionContext context, Object table, Object key) throws UnresolvedControlThrowable {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object value = t.rawget(key);

			if (value != null) {
				context.getReturnBuffer().setTo(value);
				return;
			}
			// else fall through and check the __index a metamethod
		}

		Object handler = Metatables.getMetamethod(context, Metatables.MT_INDEX, table);

		if (handler == null && table instanceof Table) {
			// key not found and no index metamethod, returning nil
			context.getReturnBuffer().setTo(null);
			return;
		}
		if (handler instanceof LuaFunction) {
			// call the handler
			LuaFunction fn = (LuaFunction) handler;

			try {
				fn.invoke(context, table, key);
				evaluateTailCalls(context);
			}
			catch (ResolvedControlThrowable ct) {
				throw ct.unresolve();
			}
		}
		else if (handler instanceof Table) {
			// TODO: protect against infinite loops
			index(context, handler, key);
		}
		else {
			throw IllegalOperationAttemptException.index(table, key);
		}
	}

	/**
	 * Evaluates the Lua expression {@code table[key]} (in non-assignment context) including
	 * the handling of metamethods, and stores the result to the return buffer associated with
	 * {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * <p>This method differs from {@link #index(ExecutionContext, Object, Object)}
	 * in that the {@code table} argument is required to be a non-{@code null} reference
	 * to a {@link Table}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param table  the table, must not be {@code null}
	 * @param key  the key, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 *
	 * @throws NullPointerException  if {@code context} or {@code table} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void index(ExecutionContext context, Table table, Object key) throws UnresolvedControlThrowable {
		// TODO: don't just delegate to the generic case
		index(context, (Object) Objects.requireNonNull(table), key);
	}

	/**
	 * Executes the Lua statement {@code table[key] = value}, including the handling of
	 * metamethods, and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param table  the target, may be any value
	 * @param key  the key, may be any value
	 * @param value  the value, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void setindex(ExecutionContext context, Object table, Object key, Object value) throws UnresolvedControlThrowable {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object r = t.rawget(key);

			if (r != null) {
				t.rawset(key, value);
				return;
			}
		}

		Object handler = Metatables.getMetamethod(context, Metatables.MT_NEWINDEX, table);

		if (handler == null && table instanceof Table) {
			Table t = (Table) table;
			t.rawset(key, value);
			return;
		}

		if (handler instanceof LuaFunction) {
			// call the handler
			LuaFunction fn = (LuaFunction) handler;

			try {
				fn.invoke(context, table, key, value);
				evaluateTailCalls(context);
			}
			catch (ResolvedControlThrowable ct) {
				throw ct.unresolve();
			}
		}
		else if (handler instanceof Table) {
			// TODO: protect against infinite loops
			setindex(context, handler, key, value);
		}
		else {
			throw IllegalOperationAttemptException.index(table, key);
		}
	}

	/**
	 * Executes the Lua statement {@code table[key] = value}, including the handling of
	 * metamethods, and stores the result to the return buffer associated with {@code context}.
	 * <b>This method throws an {@link UnresolvedControlThrowable}</b>: non-local control
	 * changes are expected to be resolved by the caller of this method.
	 *
	 * <p>This method differs from {@link #setindex(ExecutionContext, Object, Object, Object)}
	 * in that the {@code table} argument is required to be a non-{@code null} reference
	 * to a {@link Table}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param table  the target, must not be {@code null}
	 * @param key  the key, may be any value
	 * @param value  the value, may be any value
	 *
	 * @throws UnresolvedControlThrowable  if the evaluation called a metamethod and the metamethod
	 *                           initiates a non-local control change
	 * @throws NullPointerException  if {@code context} or {@code table} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static void setindex(ExecutionContext context, Table table, Object key, Object value) throws UnresolvedControlThrowable {
		// TODO: don't just delegate to the generic case
		setindex(context, (Object) Objects.requireNonNull(table), key, value);
	}

	private static final Long ZERO = Long.valueOf(0L);

	/**
	 * Returns {@code true} iff {@code a} <i>op</i> {@code b}, where <i>op</i> is
	 * "{@code <=}" (lesser than or equal to) if {@code sign > 0}, or "{@code >=}" (greater
	 * than or equal to) if {@code sign < 0}.
	 *
	 * <p>When {@code sign} is zero or <i>NaN</i>, returns {@code false}.</p>
	 *
	 * @param a  the first operand, must not be {@code null}
	 * @param b  the second operand, must not be {@code null}
	 * @param sign  the sign, must not be {@code null}
	 *
	 * @return  {@code true} iff {@code a} is below {@code b} depending on the sign
	 *          of {@code sign}
	 *
	 * @throws NullPointerException  if {@code a}, {@code b} or {@code sign} is {@code null}
	 */
	@SuppressWarnings("unused")
	public static boolean signed_le(Number a, Number b, Number sign) {
		return !eq(ZERO, sign) && (lt(ZERO, sign) ? le(a, b) : le(b, a));
	}

}
