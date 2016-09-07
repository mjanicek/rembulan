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

import net.sandius.rembulan.runtime.Dispatch;

import java.util.Collection;

/**
 * A buffer used to store and retrieve the results of function calls and Lua operations
 * initiated via the {@link Dispatch} methods.
 *
 * <p>In Lua, a function may return either a sequence of values, or a tail call
 * of another Lua function. A return buffer represents both possibilities as an optional
 * call target (for tail calls), and mandatory sequence of values (for tail calls, these
 * are the call arguments; for plain returns, these are the actual values).</p>
 *
 * <p>To query the length of the sequence, use {@link #size()}. To access the values, use
 * {@link #getAsArray()} to get the entire sequence packed into an array, {@link #get(int)}
 * to get a single value in the sequence (or {@code null} when the index is not between 0
 * (inclusive) and {@code size()} (exclusive), or {@link #get0()}, {@link #get1()}, ...
 * for possibly more optimised versions of {@code get(int)}.</p>
 *
 * <p>To determine whether the return buffer contains a tail call, the method {@link #isCall()}
 * may be used; when it returns {@code true}, then the call target (i.e. the object to
 * be tail-called) is accessible using {@link #getCallTarget()}.</p>
 *
 * <p>New return buffer instances may be created using the methods provided by the static
 * factory class {@link net.sandius.rembulan.impl.ReturnBuffers}.</p>
 */
public interface ReturnBuffer {

	/**
	 * Returns the number of values stored in this buffer.
	 *
	 * <p>When the buffer contains a tail call, this is the number of arguments to the call
	 * (i.e., the call target is not counted); otherwise, it is the number of return values.</p>
	 *
	 * @return  the number of values stored in this buffer
	 */
	@SuppressWarnings("unused")
	int size();

	/**
	 * Returns {@code true} iff this buffer contains a tail call.
	 *
	 * <p>If this method returns {@code true}, the call target may be retrieved using
	 * {@link #getCallTarget()}.</p>
	 *
	 * @return  {@code true} iff this buffer contains a tail call
	 */
	@SuppressWarnings("unused")
	boolean isCall();

	/**
	 * If this buffer contains a tail call, returns the target of the call, i.e,
	 * the object {@code f} to be called with the arguments <i>args</i>
	 * as {@code f(}<i>args</i>{@code )}.
	 *
	 * <p>If this buffer does not contain a tail call, an {@link IllegalStateException}
	 * is thrown; use {@link #isCall()} to find out whether the buffer contains
	 * a tail call.</p>
	 *
	 * <p>In order to retrieve the call arguments, use any of the value getter methods
	 * (e.g., {@link #getAsArray()}).</p>
	 *
	 * <p>Note that the target of the tail call may be any object, including {@code null}.</p>
	 *
	 * @return  the target of the tail call
	 *
	 * @throws IllegalStateException  when this buffer does not contain a tail call
	 */
	@SuppressWarnings("unused")
	Object getCallTarget();

	/**
	 * Sets the result value in this buffer to the empty sequence.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return
	 * </pre>
	 * <p>(Note, however, that this method has no influence on the control flow of its
	 * caller.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToContentsOf(new Object[] {})
	 * </pre>
	 * <p>However, implementations of this method may be optimised due to the fact
	 * that the number of values in this case is known at compile time.</p>
	 */
	@SuppressWarnings("unused")
	void setTo();

	/**
	 * Sets the result value in this buffer to a single value.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return a
	 * </pre>
	 * <p>(Note, however, that this method has no influence on the control flow of its
	 * caller.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToContentsOf(new Object[] {a})
	 * </pre>
	 * <p>However, implementations of this method may be optimised due to the fact
	 * that the number of values in this case is known at compile time.</p>
	 *
	 * <p><b>Note</b>: the argument {@code a} is taken as-is, even if {@code a} is
	 * an array or a collection. To set the result value of this buffer to the <i>contents</i>
	 * of {@code a}, use {@link #setToContentsOf(Object[])}
	 * or {@link #setToContentsOf(Collection)}.</p>
	 *
	 * @param a  the result value, may be {@code null}
	 */
	@SuppressWarnings("unused")
	void setTo(Object a);

	/**
	 * Sets the result value in this buffer to two values.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return a, b
	 * </pre>
	 * <p>(Note, however, that this method has no influence on the control flow of its
	 * caller.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToContentsOf(new Object[] {a, b})
	 * </pre>
	 * <p>However, implementations of this method may be optimised due to the fact
	 * that the number of values in this case is known at compile time.</p>
	 *
	 * @param a  the first value, may be {@code null}
	 * @param b  the second value, may be {@code null}
	 */
	@SuppressWarnings("unused")
	void setTo(Object a, Object b);

	/**
	 * Sets the result value in this buffer to three values.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return a, b, c
	 * </pre>
	 * <p>(Note, however, that this method has no influence on the control flow of its
	 * caller.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToContentsOf(new Object[] {a, b, c})
	 * </pre>
	 * <p>However, implementations of this method may be optimised due to the fact
	 * that the number of values in this case is known at compile time.</p>
	 *
	 * @param a  the first value, may be {@code null}
	 * @param b  the second value, may be {@code null}
	 * @param c  the third value, may be {@code null}
	 */
	@SuppressWarnings("unused")
	void setTo(Object a, Object b, Object c);

	/**
	 * Sets the result value in this buffer to four values.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return a, b, c, d
	 * </pre>
	 * <p>(Note, however, that this method has no influence on the control flow of its
	 * caller.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToContentsOf(new Object[] {a, b, c, d})
	 * </pre>
	 * <p>However, implementations of this method may be optimised due to the fact
	 * that the number of values in this case is known at compile time.</p>
	 *
	 * @param a  the first value, may be {@code null}
	 * @param b  the second value, may be {@code null}
	 * @param c  the third value, may be {@code null}
	 * @param d  the fourth value, may be {@code null}
	 */
	@SuppressWarnings("unused")
	void setTo(Object a, Object b, Object c, Object d);

	/**
	 * Sets the result value in this buffer to five values.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return a, b, c, d, e
	 * </pre>
	 * <p>(Note, however, that this method has no influence on the control flow of its
	 * caller.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToContentsOf(new Object[] {a, b, c, d, e})
	 * </pre>
	 * <p>However, implementations of this method may be optimised due to the fact
	 * that the number of values in this case is known at compile time.</p>
	 *
	 * @param a  the first value, may be {@code null}
	 * @param b  the second value, may be {@code null}
	 * @param c  the third value, may be {@code null}
	 * @param d  the fourth value, may be {@code null}
	 * @param e  the fifth value, may be {@code null}
	 */
	@SuppressWarnings("unused")
	void setTo(Object a, Object b, Object c, Object d, Object e);

	/**
	 * Sets the result value in this buffer to the contents of {@code array}.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return a_0, ..., a_n
	 * </pre>
	 * <p>where {@code a_i} denotes the value of {@code args[i]} and {@code n} is equal
	 * to {@code (args.length - 1)}. (Note, however, that this method has no influence on
	 * the control flow of its caller.)</p>
	 *
	 * <p>The contents of {@code array} are not modified by the buffer, and the reference
	 * to {@code array} is not retained by the buffer. (In other words,
	 * implementations of the {@code ReturnBuffer} interface are required to make a copy of
	 * {@code array}'s contents.)</p>
	 *
	 * <p>For result values of known length, it is recommended to use the appropriate
	 * {@code setTo(...)} method.</p>
	 *
	 * @param array  the array to set values from, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code array} is {@code null}
	 */
	@SuppressWarnings("unused")
	void setToContentsOf(Object[] array);

	/**
	 * Sets the result value in this buffer to the contents of {@code collection},
	 * in the order given by {@code collection}'s {@link Collection#iterator() iterator()}.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return a_0, ..., a_n
	 * </pre>
	 * <p>where {@code a_i} denotes the value returned by the {@code (i+1)}-th invocation
	 * of an iterator on {@code args}, and {@code n} is equal to {@code args.size() - 1}.
	 * (Note, however, that this method has no influence on the control flow of its caller.)</p>
	 *
	 * <p>The contents of {@code collection} are not modified by the buffer,
	 * and the reference to {@code collection} is not retained by the buffer.
	 * (In other words, implementations of the {@code ReturnBuffer} interface are required
	 * to make a copy {@code collection}'s contents.</p>
	 *
	 * <p>When the contents of {@code collection} are modified concurrently with
	 * the execution of this method, the behaviour of this method is undefined.</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToContentsOf(collection.toArray())
	 * </pre>
	 * <p>However, implementations of this method may be expected to iterate over the
	 * elements of {@code collection} directly (i.e, avoiding the conversion to array).</p>
	 *
	 * <p>For result values of known length, it is recommended to use the appropriate
	 * {@code setTo(...)} method.</p>
	 *
	 * @param collection  the collection to set values from, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code collection} is {@code null}
	 */
	@SuppressWarnings("unused")
	void setToContentsOf(Collection<?> collection);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} without arguments.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return target()
	 * </pre>
	 * <p>Note, however, that this method is not a control statement that it does <i>not</i>
	 * evaluate the call, but rather stores into the buffer a description of how the call
	 * should be made. (In order to execute such a call in a non-tail-call setting, use
	 * {@link Dispatch#call(ExecutionContext, Object)}.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToCallWithContentsOf(target, new Object[] {})
	 * </pre>
	 * <p>However, implementations of this method may optimise this method since the number
	 * of arguments is known at compile time.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 */
	@SuppressWarnings("unused")
	void setToCall(Object target);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with a single
	 * argument {@code arg1}.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return target(arg1)
	 * </pre>
	 * <p>Note, however, that this method is not a control statement that it does <i>not</i>
	 * evaluate the call, but rather stores into the buffer a description of how the call
	 * should be made. (In order to execute such a call in a non-tail-call setting, use
	 * {@link Dispatch#call(ExecutionContext, Object, Object)}.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToCallWithContentsOf(target, new Object[] {arg1})
	 * </pre>
	 * <p>However, implementations of this method may optimise this method since the number
	 * of arguments is known at compile time.</p>
	 *
	 * <p><b>Note</b>: the argument {@code arg1} is taken as-is, even if {@code arg1} is
	 * an array or a collection. To set the call arguments to the <i>contents</i>
	 * of {@code arg1}, use {@link #setToCallWithContentsOf(Object, Object[])}}
	 * or {@link #setToCallWithContentsOf(Object, Collection)}}.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param arg1  the first call argument, may be {@code null}
	 */
	@SuppressWarnings("unused")
	void setToCall(Object target, Object arg1);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with the arguments
	 * {@code arg1} and {@code arg2}.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return target(arg1, arg2)
	 * </pre>
	 * <p>Note, however, that this method is not a control statement that it does <i>not</i>
	 * evaluate the call, but rather stores into the buffer a description of how the call
	 * should be made. (In order to execute such a call in a non-tail-call setting, use
	 * {@link Dispatch#call(ExecutionContext, Object, Object, Object)}.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToCallWithContentsOf(target, new Object[] {arg1, arg2})
	 * </pre>
	 * <p>However, implementations of this method may optimise this method since the number
	 * of arguments is known at compile time.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param arg1  the first call argument, may be {@code null}
	 * @param arg2  the second call argument, may be {@code null}
	 */
	@SuppressWarnings("unused")
	void setToCall(Object target, Object arg1, Object arg2);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with the arguments
	 * {@code arg1}, {@code arg2} and {@code arg3}.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return target(arg1, arg2, arg3)
	 * </pre>
	 * <p>Note, however, that this method is not a control statement that it does <i>not</i>
	 * evaluate the call, but rather stores into the buffer a description of how the call
	 * should be made. (In order to execute such a call in a non-tail-call setting, use
	 * {@link Dispatch#call(ExecutionContext, Object, Object, Object, Object)}.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToCallWithContentsOf(target, new Object[] {arg1, arg2, arg3})
	 * </pre>
	 * <p>However, implementations of this method may optimise this method since the number
	 * of arguments is known at compile time.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param arg1  the first call argument, may be {@code null}
	 * @param arg2  the second call argument, may be {@code null}
	 * @param arg3  the third call argument, may be {@code null}
	 */
	@SuppressWarnings("unused")
	void setToCall(Object target, Object arg1, Object arg2, Object arg3);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with the arguments
	 * {@code arg1}, {@code arg2}, {@code arg3} and {@code arg4}.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return target(arg1, arg2, arg3, arg4)
	 * </pre>
	 * <p>Note, however, that this method is not a control statement that it does <i>not</i>
	 * evaluate the call, but rather stores into the buffer a description of how the call
	 * should be made. (In order to execute such a call in a non-tail-call setting, use
	 * {@link Dispatch#call(ExecutionContext, Object, Object, Object, Object, Object)}.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToCallWithContentsOf(target, new Object[] {arg1, arg2, arg3, arg4})
	 * </pre>
	 * <p>However, implementations of this method may optimise this method since the number
	 * of arguments is known at compile time.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param arg1  the first call argument, may be {@code null}
	 * @param arg2  the second call argument, may be {@code null}
	 * @param arg3  the third call argument, may be {@code null}
	 * @param arg4  the fourth call argument, may be {@code null}
	 */
	@SuppressWarnings("unused")
	void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with the arguments
	 * {@code arg1}, {@code arg2}, {@code arg3}, {@code arg4} and {@code arg5}.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return target(arg1, arg2, arg3, arg4, arg5)
	 * </pre>
	 * <p>Note, however, that this method is not a control statement that it does <i>not</i>
	 * evaluate the call, but rather stores into the buffer a description of how the call
	 * should be made. (In order to execute such a call in a non-tail-call setting, use
	 * {@link Dispatch#call(ExecutionContext, Object, Object, Object, Object, Object)}.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToCallWithContentsOf(target, new Object[] {arg1, arg2, arg3, arg4, arg5})
	 * </pre>
	 * <p>However, implementations of this method may optimise this method since the number
	 * of arguments is known at compile time.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param arg1  the first call argument, may be {@code null}
	 * @param arg2  the second call argument, may be {@code null}
	 * @param arg3  the third call argument, may be {@code null}
	 * @param arg4  the fourth call argument, may be {@code null}
	 * @param arg5  the fifth call argument, may be {@code null}
	 */
	@SuppressWarnings("unused")
	void setToCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with the contents
	 * of the array {@code args} used as call arguments.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return target(a_0, ..., a_n)
	 * </pre>
	 * <p>where {@code a_i} denotes the value of {@code args[i]} and {@code n} is equal
	 * to {@code (args.length - 1)}.
	 * Note, however, that this method is not a control statement that it does <i>not</i>
	 * evaluate the call, but rather stores into the buffer a description of how the call
	 * should be made. (In order to execute such a call in a non-tail-call setting, use
	 * {@link Dispatch#call(ExecutionContext, Object, Object[])}.)</p>
	 *
	 * <p>The contents of {@code args} are not modified by the buffer, and the reference
	 * to {@code args} is not retained by the buffer. (In other words,
	 * implementations of the {@code ReturnBuffer} interface are required to make copy of
	 * {@code args}'s contents.)</p>
	 *
	 * <p>For tail calls with a fixed number of arguments known at compile time, it is
	 * recommended to use the appropriate {@code setToCall(...)} method.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param args  the array to set values from, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code args} is {@code null}
	 */
	void setToCallWithContentsOf(Object target, Object[] args);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with the contents
	 * of the collection {@code args} used as call arguments.
	 *
	 * <p>This equivalent to the assignment of return values by the Lua statement</p>
	 * <pre>
	 *     return target(a_0, ..., a_n)
	 * </pre>
	 * <p>where {@code a_i} denotes the value returned by the {@code (i+1)}-th invocation
	 * of an iterator on {@code args}, and {@code n} is equal to {@code args.size() - 1}.
	 * Note, however, that this method is not a control statement that it does <i>not</i>
	 * evaluate the call, but rather stores into the buffer a description of how the call
	 * should be made. (In order to execute such a call in a non-tail-call setting, use
	 * {@link Dispatch#call(ExecutionContext, Object, Object[])}.)</p>
	 *
	 * <p>The contents of {@code args} are not modified by the buffer, and the reference
	 * to {@code args} is not retained by the buffer. (In other words,
	 * implementations of the {@code ReturnBuffer} interface are required to make copy of
	 * {@code args}'s contents.)</p>
	 *
	 * <p>The effect of this method is equivalent to</p>
	 * <pre>
	 *     setToTailCallWithContentsOf(target, args.toArray())
	 * </pre>
	 * <p>However, implementations of this method may be expected to iterate over the
	 * elements of {@code collection} directly (i.e, avoiding the conversion to array).</p>
	 *
	 * <p>For tail calls with a fixed number of arguments known at compile time, it is
	 * recommended to use the appropriate {@code setToCall(...)} method.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param args  the array to set values from, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code args} is {@code null}
	 */
	@SuppressWarnings("unused")
	void setToCallWithContentsOf(Object target, Collection<?> args);

	/**
	 * Returns the values stored in this buffer as an array.
	 *
	 * <p>When this buffer is a tail call, the result will be the arguments of the call;
	 * otherwise, the result are the return values.</p>
	 *
	 * <p>The resulting array is freshly allocated, and the reference to it is not retained
	 * by the buffer. It may therefore be used freely (no defensive copying is required).</p>
	 *
	 * <p>This method's functionality is equivalent to the following:</p>
	 * <pre>
	 *     public Object[] getAsArray() {
	 *         Object[] result = new Object[size()];
	 *         for (int i = 0; i &lt; size(); i++) {
	 *             result[i] = get(i);
	 *         }
	 *         return result;
	 *     }
	 * </pre>
	 * <p>(Implementations of this interface may of course provide a more optimised
	 * implementation of this method.)</p>
	 *
	 * @return  values stored in this buffer as an array, guaranteed to be non-{@code null}
	 */
	Object[] getAsArray();

	/**
	 * Returns the {@code idx}-th value stored in this buffer, or {@code null} if there is
	 * no value with the given index {@code idx}.
	 *
	 * <p>Indices start at 0 like in Java, i.e. 0 is the index of the first value.</p>
	 *
	 * <p>When this buffer is a tail call, the result is the {@code idx}-th argument of
	 * the call; otherwise, it is the {@code idx}-th return value.</p>
	 *
	 * <p>A {@code null} result may be an indication of an index out of bounds, or that
	 * the value at the {@code idx}-th position is {@code null}. In order to distinguish
	 * these two cases, an explicit query of the {@link #size()} must be used.</p>
	 *
	 * <p>Note that when the index is known at compile time, it is recommended to use the
	 * appropriate optimised getter method (e.g., {@link #get0()} for accessing the first
	 * value).</p>
	 *
	 * @param idx  the index of the value to be retrieved
	 * @return  the {@code idx}-th value stored in the buffer (may be {@code null}),
	 *          or {@code null} if there is no value with index {@code idx}
	 */
	Object get(int idx);

	/**
	 * Returns the first value stored in this buffer.
	 *
	 * <p>The effect of this method is equivalent to {@code get(0)}; however, implementations
	 * of this interface may optimise this method since the index is known at compile
	 * time.</p>
	 *
	 * <p>When this buffer is a tail call, the result is the first argument of
	 * the call; otherwise, it is the first return value.</p>
	 *
	 * @return  the value of the first value stored in this buffer (may be {@code null}),
	 *          or {@code null} if this buffer is empty
	 */
	Object get0();

	/**
	 * Returns the second value stored in this buffer.
	 *
	 * <p>The effect of this method is equivalent to {@code get(1)}; however, implementations
	 * of this interface may optimise this method since the index is known at compile
	 * time.</p>
	 *
	 * <p>When this buffer is a tail call, the result is the second argument of
	 * the call; otherwise, it is the second return value.</p>
	 *
	 * @return  the value of the second value stored in this buffer (may be {@code null}),
	 *          or {@code null} if this buffer's size is empty
	 */
	Object get1();

	/**
	 * Returns the third value stored in this buffer.
	 *
	 * <p>The effect of this method is equivalent to {@code get(2)}; however, implementations
	 * of this interface may optimise this method since the index is known at compile
	 * time.</p>
	 *
	 * <p>When this buffer is a tail call, the result is the third argument of
	 * the call; otherwise, it is the third return value.</p>
	 *
	 * @return  the value of the third value stored in this buffer (may be {@code null}),
	 *          or {@code null} if this buffer's size is empty
	 */
	Object get2();

	/**
	 * Returns the fourth value stored in this buffer.
	 *
	 * <p>The effect of this method is equivalent to {@code get(3)}; however, implementations
	 * of this interface may optimise this method since the index is known at compile
	 * time.</p>
	 *
	 * <p>When this buffer is a tail call, the result is the fourth argument of
	 * the call; otherwise, it is the fourth return value.</p>
	 *
	 * @return  the value of the fourth value stored in this buffer (may be {@code null}),
	 *          or {@code null} if this buffer's size is empty
	 */
	Object get3();

	/**
	 * Returns the fifth value stored in this buffer.
	 *
	 * <p>The effect of this method is equivalent to {@code get(4)}; however, implementations
	 * of this interface may optimise this method since the index is known at compile
	 * time.</p>
	 *
	 * <p>When this buffer is a tail call, the result is the fifth argument of
	 * the call; otherwise, it is the fifth return value.</p>
	 *
	 * @return  the value of the fifth value stored in this buffer (may be {@code null}),
	 *          or {@code null} if this buffer's size is empty
	 */
	Object get4();

}
