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

/**
 * TODO: summary
 */
public interface ReturnBuffer {

	/**
	 * Return the number of values stored in this buffer.
	 *
	 * <p>When the buffer contains a tail call, this is the number of arguments to the call;
	 * otherwise, it is the number of return values.</p>
	 *
	 * @return  the number of values stored in this buffer
	 */
	int size();

	/**
	 * Returns {@code true} iff this buffer contains a tail call.
	 *
	 * @return  {@code true} iff this buffer contains a tail call
	 */
	boolean isTailCall();

	/**
	 * If this buffer contains a tail call, returns the target of the call (e.g.
	 * the function to be tail called).
	 *
	 * <p>If this buffer does not contain a tail call, an {@link IllegalStateException}
	 * is thrown; use {@link #isTailCall()} to find out whether the buffer contains
	 * a tail call.</p>
	 *
	 * <p>Note that the target of the tail call may be any object, including {@code null}.
	 * </p>
	 *
	 * @return  the target of the tail call
	 *
	 * @throws IllegalStateException  when this buffer does not contain a tail call
	 */
	Object getTailCallTarget();

	/**
	 * Sets the result value in this buffer to the empty sequence.
	 *
	 * <p>The effect of this method is equivalent to {@code setToArray(new Object[] {})};
	 * however, implementations of this interface may be optimised due to the fact
	 * that the number of values in this case is known at compile time.</p>
	 */
	void setTo();

	/**
	 * Sets the result value in this buffer to a single value.
	 *
	 * <p>The effect of this method is equivalent to {@code setToArray(new Object[] {a})};
	 * however, implementations of this interface may be optimised due to the fact
	 * that the number of values in this case is known at compile time.</p>
	 *
	 * @param a  the result value, may be {@code null}
	 */
	void setTo(Object a);

	/**
	 * Sets the result value in this buffer to two values.
	 *
	 * <p>The effect of this method is equivalent to {@code setToArray(new Object[] {a, b})};
	 * however, implementations of this interface may be optimised due to the fact
	 * that the number of values in this case is known at compile time.</p>
	 *
	 * @param a  the first value, may be {@code null}
	 * @param b  the second value, may be {@code null}
	 */
	void setTo(Object a, Object b);

	/**
	 * Sets the result value in this buffer to three values.
	 *
	 * <p>The effect of this method is equivalent to
	 * {@code setToArray(new Object[] {a, b, c})}; however, implementations of this interface
	 * may be optimised due to the fact that the number of values in this case is known
	 * at compile time.</p>
	 *
	 * @param a  the first value, may be {@code null}
	 * @param b  the second value, may be {@code null}
	 * @param c  the third value, may be {@code null}
	 */
	void setTo(Object a, Object b, Object c);

	/**
	 * Sets the result value in this buffer to four values.
	 *
	 * <p>The effect of this method is equivalent to
	 * {@code setToArray(new Object[] {a, b, c, d})}; however, implementations of this
	 * interface may be optimised due to the fact that the number of values in this case
	 * is known at compile time.</p>
	 *
	 * @param a  the first value, may be {@code null}
	 * @param b  the second value, may be {@code null}
	 * @param c  the third value, may be {@code null}
	 * @param d  the fourth value, may be {@code null}
	 */
	void setTo(Object a, Object b, Object c, Object d);

	/**
	 * Sets the result value in this buffer to five values.
	 *
	 * <p>The effect of this method is equivalent to
	 * {@code setToArray(new Object[] {a, b, c, d, e})}; however, implementations of this
	 * interface may be optimised due to the fact that the number of values in this case
	 * is known at compile time.</p>
	 *
	 * @param a  the first value, may be {@code null}
	 * @param b  the second value, may be {@code null}
	 * @param c  the third value, may be {@code null}
	 * @param d  the fourth value, may be {@code null}
	 * @param e  the fifth value, may be {@code null}
	 */
	void setTo(Object a, Object b, Object c, Object d, Object e);

	/**
	 * Sets the result value in this buffer to the contents of {@code array}.
	 *
	 * <p>The contents of {@code array} are not modified by the buffer, and the reference
	 * to {@code array} is not retained by the buffer. (In other words,
	 * implementations of the {@code ReturnBuffer} interface are required to make copy of
	 * {@code array}'s contents.)</p>
	 *
	 * <p>For result values of known length, it is recommended to use the appropriate
	 * {@code setTo(...)} method.</p>
	 *
	 * @param array  the array to set values from, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code array} is {@code null}
	 */
	void setToArray(Object[] array);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} without arguments,
	 * i.e., to {@code target()}.
	 *
	 * <p>The effect of this method is equivalent to {@code tailCall(target, new Object[] {})};
	 * however, implementations of this interface may optimise this method since the number
	 * of arguments is known at compile time.</p>
	 *
	 * <p>Note that this method does <i>not</i> evaluate the tail call, but is rather
	 * the <i>specification</i> of the call.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 */
	void tailCall(Object target);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with a single
	 * argument {@code arg1}, i.e., to {@code target(arg1)}.
	 *
	 * <p>The effect of this method is equivalent to
	 * {@code tailCall(target, new Object[] {arg1})}; however, implementations of this
	 * interface may optimise this method  since the number of arguments is known at compile
	 * time.</p>
	 *
	 * <p>Note that this method does <i>not</i> evaluate the tail call, but is rather
	 * the <i>specification</i> of the call.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param arg1  the first call argument, may be {@code null}
	 */
	void tailCall(Object target, Object arg1);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with the arguments
	 * {@code arg1} and {@code arg2}, i.e., to {@code target(arg1, arg2)}.
	 *
	 * <p>The effect of this method is equivalent to
	 * {@code tailCall(target, new Object[] {arg1, arg2})}; however, implementations of this
	 * interface may optimise this method since the number of arguments is known at compile
	 * time.</p>
	 *
	 * <p>Note that this method does <i>not</i> evaluate the tail call, but is rather
	 * the <i>specification</i> of the call.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param arg1  the first call argument, may be {@code null}
	 * @param arg2  the second call argument, may be {@code null}
	 */
	void tailCall(Object target, Object arg1, Object arg2);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with the arguments
	 * {@code arg1}, {@code arg2} and {@code arg3}, i.e.,
	 * to {@code target(arg1, arg2, arg3)}.
	 *
	 * <p>The effect of this method is equivalent to
	 * {@code tailCall(target, new Object[] {arg1, arg2, arg3})}; however, implementations
	 * of this interface may optimise this method since the number of arguments is known
	 * at compile time.</p>
	 *
	 * <p>Note that this method does <i>not</i> evaluate the tail call, but is rather
	 * the <i>specification</i> of the call.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param arg1  the first call argument, may be {@code null}
	 * @param arg2  the second call argument, may be {@code null}
	 * @param arg3  the third call argument, may be {@code null}
	 */
	void tailCall(Object target, Object arg1, Object arg2, Object arg3);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with the arguments
	 * {@code arg1}, {@code arg2}, {@code arg3} and {@code arg4}, i.e.,
	 * to {@code target(arg1, arg2, arg3, arg4)}.
	 *
	 * <p>The effect of this method is equivalent to
	 * {@code tailCall(target, new Object[] {arg1, arg2, arg3, arg4})}; however,
	 * implementations of this method may optimise this method since the number of arguments
	 * is known at compile time.</p>
	 *
	 * <p>Note that this method does <i>not</i> evaluate the tail call, but is rather
	 * the <i>specification</i> of the call.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param arg1  the first call argument, may be {@code null}
	 * @param arg2  the second call argument, may be {@code null}
	 * @param arg3  the third call argument, may be {@code null}
	 * @param arg4  the fourth call argument, may be {@code null}
	 */
	void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with the arguments
	 * {@code arg1}, {@code arg2}, {@code arg3}, {@code arg4} and {@code arg5} , i.e.,
	 * to {@code target(arg1, arg2, arg3, arg4, arg5)}.
	 *
	 * <p>The effect of this method is equivalent to
	 * {@code tailCall(target, new Object[] {arg1, arg2, arg3, arg4, arg5})}; however,
	 * implementations of this method may optimise this method since the number of arguments
	 * is known at compile time.</p>
	 *
	 * <p>Note that this method does <i>not</i> evaluate the tail call, but is rather
	 * the <i>specification</i> of the call.</p>
	 *
	 * @param target  tail call target, may be {@code null}
	 * @param arg1  the first call argument, may be {@code null}
	 * @param arg2  the second call argument, may be {@code null}
	 * @param arg3  the third call argument, may be {@code null}
	 * @param arg4  the fourth call argument, may be {@code null}
	 * @param arg5  the fifth call argument, may be {@code null}
	 */
	void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5);

	/**
	 * Sets the result in this buffer to a tail call of {@code target} with the arguments
	 * {@code args} passed as an array.
	 *
	 * <p>Conceptually, this corresponds to the call</p>
	 * <pre>
	 *     target(a_0, ..., a_n)
	 * </pre>
	 * <p>where {@code a_i} denotes the value of {@code args[i]} and {@code n} is equal
	 * to {@code args.length}.</p>
	 *
	 * <p>The contents of {@code args} are not modified by the buffer, and the reference
	 * to {@code args} is not retained by the buffer. (In other words,
	 * implementations of the {@code ReturnBuffer} interface are required to make copy of
	 * {@code args}'s contents.)</p>
	 *
	 * <p>For tail calls with a fixed number of arguments known at compile time, it is
	 * recommended to use the appropriate {@code tailCall(...)} method.</p>
	 *
	 * <p>Note that this method does <i>not</i> evaluate the tail call, but is rather
	 * the <i>specification</i> of the call.</p>
	 *
	 * @param args  the array to set values from, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code args} is {@code null}
	 */
	void tailCall(Object target, Object[] args);

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
	 *     public Object[] toArray() {
	 *         Object[] result = new Object[size()];
	 *         for (int i = 0; i < size(); i++) {
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
	Object[] toArray();

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
	 * appropriate optimised getter method (e.g., {@link #_0()} for accessing the first
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
	Object _0();

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
	Object _1();

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
	Object _2();

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
	Object _3();

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
	Object _4();

}
