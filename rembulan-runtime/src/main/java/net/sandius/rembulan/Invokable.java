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

import net.sandius.rembulan.exec.ResolvedControlThrowable;
import net.sandius.rembulan.exec.UnresolvedControlThrowable;

/**
 * An invokable object, corresponding to the Lua {@code function} type.
 */
public interface Invokable {

	/**
	 * Invokes this object in the given execution context {@code context} without arguments.
	 * The result of the invocation will be stored in the return buffer of the execution context.
	 * <b>This method throws a {@link UnresolvedControlThrowable}</b>; the throwable should
	 * be caught, handled and re-thrown by the caller of this method.
	 *
	 * <p><b>This method is not meant to be invoked directly by client code</b>, since
	 * the invocation does not handle possible tail call returns.
	 * Unless it is known that this method does not perform a tail call,
	 * use {@link Dispatch#call(ExecutionContext, Object)} instead.</p>
	 *
	 * <p>The behaviour of this method is undefined when {@code context} is {@code null}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 *
	 * @throws ResolvedControlThrowable  if the call initiates a non-local control change
	 */
	void invoke(ExecutionContext context)
			throws ResolvedControlThrowable;

	/**
	 * Invokes this object in the given execution context {@code context} with a single
	 * argument {@code arg1}.
	 * The result of the invocation will be stored in the return buffer of the execution context.
	 * <b>This method throws a {@link UnresolvedControlThrowable}</b>; the throwable should
	 * be caught, handled and re-thrown by the caller of this method.
	 *
	 * <p><b>This method is not meant to be invoked directly by client code</b>, since
	 * the invocation does not handle possible tail call returns.
	 * Unless it is known that this method does not perform a tail call,
	 * use {@link Dispatch#call(ExecutionContext, Object, Object)} instead.</p>
	 *
	 * <p>The behaviour of this method is undefined when {@code context} is {@code null}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param arg1  call argument, may be {@code null}
	 *
	 * @throws ResolvedControlThrowable  if the call initiates a non-local control change
	 */
	void invoke(ExecutionContext context, Object arg1)
			throws ResolvedControlThrowable;

	/**
	 * Invokes this object in the given execution context {@code context} with two arguments,
	 * {@code arg1} and {@code arg2}.
	 * The result of the invocation will be stored in the return buffer of the execution context.
	 * <b>This method throws a {@link UnresolvedControlThrowable}</b>; the throwable should
	 * be caught, handled and re-thrown by the caller of this method.
	 *
	 * <p><b>This method is not meant to be invoked directly by client code</b>, since
	 * the invocation does not handle possible tail call returns.
	 * Unless it is known that this method does not perform a tail call,
	 * use {@link Dispatch#call(ExecutionContext, Object, Object, Object)} instead.</p>
	 *
	 * <p>The behaviour of this method is undefined when {@code context} is {@code null}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param arg1  the first argument, may be {@code null}
	 * @param arg2  the second argument, may be {@code null}
	 *
	 * @throws ResolvedControlThrowable  if the call initiates a non-local control change
	 */
	void invoke(ExecutionContext context, Object arg1, Object arg2)
			throws ResolvedControlThrowable;

	/**
	 * Invokes this object in the given execution context {@code context} with three arguments,
	 * {@code arg1}, {@code arg2} and {@code arg3}.
	 * The result of the invocation will be stored in the return buffer of the execution context.
	 * <b>This method throws a {@link UnresolvedControlThrowable}</b>; the throwable should
	 * be caught, handled and re-thrown by the caller of this method.
	 *
	 * <p><b>This method is not meant to be invoked directly by client code</b>, since
	 * the invocation does not handle possible tail call returns.
	 * Unless it is known that this method does not perform a tail call,
	 * use {@link Dispatch#call(ExecutionContext, Object, Object, Object, Object)}
	 * instead.</p>
	 *
	 * <p>The behaviour of this method is undefined when {@code context} is {@code null}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param arg1  the first argument, may be {@code null}
	 * @param arg2  the second argument, may be {@code null}
	 * @param arg3  the third argument, may be {@code null}
	 *
	 * @throws ResolvedControlThrowable  if the call initiates a non-local control change
	 */
	void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3)
			throws ResolvedControlThrowable;

	/**
	 * Invokes this object in the given execution context {@code context} with four arguments,
	 * {@code arg1}, {@code arg2}, {@code arg3} and {@code arg4}.
	 * The result of the invocation will be stored in the return buffer of the execution context.
	 * <b>This method throws a {@link UnresolvedControlThrowable}</b>; the throwable should
	 * be caught, handled and re-thrown by the caller of this method.
	 *
	 * <p><b>This method is not meant to be invoked directly by client code</b>, since
	 * the invocation does not handle possible tail call returns.
	 * Unless it is known that this method does not perform a tail call,
	 * use {@link Dispatch#call(ExecutionContext, Object, Object, Object, Object, Object)}
	 * instead.</p>
	 *
	 * <p>The behaviour of this method is undefined when {@code context} is {@code null}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param arg1  the first argument, may be {@code null}
	 * @param arg2  the second argument, may be {@code null}
	 * @param arg3  the third argument, may be {@code null}
	 * @param arg4  the fourth argument, may be {@code null}
	 *
	 * @throws ResolvedControlThrowable  if the call initiates a non-local control change
	 */
	void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4)
			throws ResolvedControlThrowable;

	/**
	 * Invokes this object in the given execution context {@code context} with five arguments,
	 * {@code arg1}, {@code arg2}, {@code arg3}, {@code arg4} and {@code arg5}.
	 * The result of the invocation will be stored in the return buffer of the execution context.
	 * <b>This method throws a {@link UnresolvedControlThrowable}</b>; the throwable should
	 * be caught, handled and re-thrown by the caller of this method.
	 *
	 * <p><b>This method is not meant to be invoked directly by client code</b>, since
	 * the invocation does not handle possible tail call returns.
	 * Unless it is known that this method does not perform a tail call,
	 * use {@link Dispatch#call(ExecutionContext, Object, Object, Object, Object, Object, Object)}
	 * instead.</p>
	 *
	 * <p>The behaviour of this method is undefined when {@code context} is {@code null}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param arg1  the first argument, may be {@code null}
	 * @param arg2  the second argument, may be {@code null}
	 * @param arg3  the third argument, may be {@code null}
	 * @param arg4  the fourth argument, may be {@code null}
	 * @param arg5  the fifth argument, may be {@code null}
	 *
	 * @throws ResolvedControlThrowable  if the call initiates a non-local control change
	 */
	void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5)
			throws ResolvedControlThrowable;

	/**
	 * Invokes this object in the given execution context {@code context} with the call
	 * arguments stored in the array {@code args}.
	 * The result of the invocation will be stored in the return buffer of the execution context.
	 * <b>This method throws a {@link UnresolvedControlThrowable}</b>; the throwable should
	 * be caught, handled and re-thrown by the caller of this method.
	 *
	 * <p><b>This method is not meant to be invoked directly by client code</b>, since
	 * the invocation does not handle possible tail call returns.
	 * Unless it is known that this method does not perform a tail call,
	 * use {@link Dispatch#call(ExecutionContext, Object, Object[])}
	 * instead.</p>
	 *
	 * <p>The contents of the array {@code args} must not be modified by this method,
	 * and the reference to {@code args} must not be retained by the invokable or any other
	 * objects the invokable interacts with beyond the scope of this method's invocation.
	 * In particular, the array reference must not be part of the suspended state created
	 * on a non-local control change.</p>
	 *
	 * <p>The behaviour of this method is undefined when {@code context} or {@code args}
	 * is {@code null}.</p>
	 *
	 * @param context  execution context, must not be {@code null}
	 * @param args  call arguments, must not be {@code null}
	 *
	 * @throws ResolvedControlThrowable  if the call initiates a non-local control change
	 */
	void invoke(ExecutionContext context, Object[] args)
			throws ResolvedControlThrowable;

}
