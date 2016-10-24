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

package net.sandius.rembulan.lib;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.MetatableProvider;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Userdata;
import net.sandius.rembulan.ValueTypeNamer;
import net.sandius.rembulan.runtime.Coroutine;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.util.Check;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import static net.sandius.rembulan.LuaFormat.TYPENAME_FUNCTION;
import static net.sandius.rembulan.LuaFormat.TYPENAME_NUMBER;
import static net.sandius.rembulan.LuaFormat.TYPENAME_STRING;
import static net.sandius.rembulan.LuaFormat.TYPENAME_TABLE;
import static net.sandius.rembulan.LuaFormat.TYPENAME_THREAD;
import static net.sandius.rembulan.LuaFormat.TYPENAME_USERDATA;

/**
 * An iterator over arguments passed to a function.
 *
 * <p>The class implements the {@link Iterator} interface, but additionally provides
 * facilities for rewinding the iterator and specialised methods for retrieving
 * and verifying argument types and/or values, named {@code nextX(...)}, where {@code X}
 * the expected next argument.
 * The argument list is immutable: invoking {@link #remove()} throws an
 * {@link UnsupportedOperationException}.</p>
 *
 * <p><b>Note:</b> references to instances of this class should not be retained by invoked
 * functions beyond the scope of an invoke or resume, since this class may hold a reference to
 * an {@link net.sandius.rembulan.runtime.ExecutionContext ExecutionContext}.</p>
 */
public class ArgumentIterator implements Iterator<Object> {

	// TODO: clean up!

	private final ValueTypeNamer namer;

	private final String name;
	private final Object[] args;
	private int index;

	private ArgumentIterator(ValueTypeNamer namer, String name, Object[] args, int index) {
		this.namer = Objects.requireNonNull(namer);
		this.name = Objects.requireNonNull(name);
		this.args = Objects.requireNonNull(args);
		this.index = Check.nonNegative(index);
	}

	ArgumentIterator(ValueTypeNamer namer, String name, Object[] args) {
		this(namer, name, args, 0);
	}

	/**
	 * Returns a new argument iterator over the argument array {@code args} that uses
	 * {@code metatableProvider} to access value names (by looking up their
	 * {@link BasicLib#MT_NAME <code>"__name"</code>} metatable field), and uses
	 * {@code name} as the name of the function for error reporting.
	 *
	 * <p>This method makes a copy of the argument array {@code args}.</p>
	 *
	 * @param metatableProvider  the metatable provider, must not be {@code null}
	 * @param name  the function name, must not be {@code null}
	 * @param args  the argument list, must not be {@code null}
	 * @return  an argument iterator over {@code args}
	 *
	 * @throws NullPointerException  if {@code metatableProvider}, {@code name} or {@code args}
	 *                               is {@code null}
	 */
	public static ArgumentIterator of(MetatableProvider metatableProvider, String name, Object[] args) {
		return new ArgumentIterator(
				new NameMetamethodValueTypeNamer(metatableProvider),
				name,
				Arrays.copyOf(args, args.length));
	}

	@Override
	public boolean hasNext() {
		return index < args.length;
	}

	@Override
	public Object next() {
		Object o = peek();
		skip();
		return o;
	}

	/**
	 * Throws an {@link UnsupportedOperationException}, since the argument list
	 * is immutable.
	 *
	 * @throws UnsupportedOperationException  every time called
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the current position in the argument list.
	 *
	 * <p>The current position is the index of the next argument as it would be returned
	 * by {@link #next()} or any other {@code nextX(...)} method if successful (i.e., if
	 * the method does not throw an exception).
	 * The index is 0-based, i.e., the first argument in the list is at position 0.</p>
	 *
	 * @return  the current position in the argument list
	 */
	public int position() {
		return index;
	}

	/**
	 * Returns the number of arguments in the argument list.
	 *
	 * @return  the number of arguments in the argument list
	 */
	public int size() {
		return args.length;
	}

	/**
	 * Returns the number of times the method {@link #next()} can be called without
	 * throwing an exception.
	 *
	 * @return  the (non-negative) number of arguments remaining in the list
	 */
	public int remaining() {
		return Math.max(args.length - index, 0);
	}


	/**
	 * Skips the next argument.
	 */
	public void skip() {
		index += 1;
		if (index < 0) {
			throw new IllegalStateException("index overflow");
		}
	}

	/**
	 * Sets the current position to {@code index}.
	 *
	 * <p>No range checking is performed: it is permissible for {@code index} to be
	 * a position outside of the argument list bounds.</p>
	 *
	 * <p>The index is 0-based, i.e., the first argument in the list is at position 0.</p>
	 *
	 * @param index  the new current position, must be non-negative
	 *
	 * @throws IllegalArgumentException  if {@code index} is negative
	 */
	public void goTo(int index) {
		this.index = Check.nonNegative(index);
	}

	/**
	 * Rewinds the iterator, moving the next pointer to the beginning of the argument
	 * list.
	 */
	public void rewind() {
		goTo(0);
	}

	/**
	 * Returns all arguments in the argument list in a freshly-allocated array.
	 *
	 * @return  an array containing all arguments
	 */
	public Object[] copyAll() {
		return Arrays.copyOf(args, args.length);
	}

	/**
	 * Returns the arguments from the current position to the end of the argument list
	 * in a freshly-allocated array.
	 *
	 * <p>If the current position is outside of the argument list, returns an empty
	 * array.</p>
	 *
	 * @return  an array containing the remaining arguments
	 */
	public Object[] copyRemaining() {
		return index <= args.length
				? Arrays.copyOfRange(args, index, args.length)
				: new Object[0];
	}

	private BadArgumentException badArgument(Throwable cause) {
		return new BadArgumentException(index + 1, name, cause);
	}

	/**
	 * Returns the argument at current position, without incrementing the current position
	 * afterwards.
	 *
	 * <p>Note that the value returned by this method may be {@code null}; if there is
	 * no argument at current position, a {@link NoSuchElementException} is thrown.</p>
	 *
	 * @return  the argument at current position, possibly {@code null}
	 *
	 * @throws NoSuchElementException  if there is no argument at the current position
	 */
	public Object peek() {
		if (index < args.length) {
			return args[index];
		}
		else {
			throw new NoSuchElementException("value expected");
		}
	}

	private Object peek(String expectedType) {
		try {
			return peek();
		}
		catch (NoSuchElementException ex) {
			throw new UnexpectedArgumentException(expectedType, "no value");
		}
	}

	// guaranteed not to return null
	private Number peekNumber() {
		Object arg = peek(TYPENAME_NUMBER.toString());
		Number n = Conversions.numericalValueOf(arg);
		if (n != null) {
			return n;
		}
		else {
			throw new UnexpectedArgumentException(TYPENAME_NUMBER.toString(), namer.typeNameOf(arg).toString());
		}
	}

	/**
	 * Retrieves the argument {@code o} at the current position, advances the current position
	 * and returns {@code o}. If there is no argument at the current position, throws
	 * a {@link BadArgumentException}; in such case, the current position is not advanced.
	 *
	 * <p>This method differs from {@link #next()} in that it signals errors using
	 * {@code BadArgumentException} rather than {@code NoSuchElementException}.</p>
	 *
	 * @return  the argument the at current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position
	 */
	public Object nextAny() {
		final Object result;
		try {
			result = peek();
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	/**
	 * Retrieves the {@linkplain Conversions#booleanValueOf(Object) boolean value} {@code b}
	 * of the argument {@code o} at the current position, advances the current position
	 * and returns {@code b}.
	 *
	 * <p>If there is no argument at the current position, throws a {@link BadArgumentException}.
	 * In that case, the current position is not advanced.</p>
	 *
	 * @return  the boolean value of the argument at current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position
	 */
	public boolean nextBoolean() {
		final boolean result;
		try {
			result = Conversions.booleanValueOf(peek());
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	/**
	 * Retrieves the {@linkplain Conversions#numericalValueOf(Object) numerical value} {@code n}
	 * of the argument {@code o} at the current position, advances the current position
	 * and returns {@code n}.
	 *
	 * <p>If there is no argument at the current position or the argument {@code o} does not
	 * have a numerical value, throws a {@link BadArgumentException}. In that case, the current
	 * position is not advanced.</p>
	 *
	 * @return  the numerical value of the argument at current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position has no numerical value
	 */
	public Number nextNumber() {
		final Number result;
		try {
			result = peekNumber();
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	/**
	 * Retrieves the {@linkplain Conversions#integerValueOf(Object) integer value} {@code i}
	 * of the argument {@code o} at the current position, advances the current position
	 * and returns {@code i}.
	 *
	 * <p>If there is no argument at the current position or the argument {@code o} does not
	 * have an integer value, throws a {@link BadArgumentException}. In that case, the current
	 * position is not advanced.</p>
	 *
	 * @return  the integer value of the argument at current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position has no integer value
	 */
	public long nextInteger() {
		final long result;
		try {
			result = Conversions.toIntegerValue(peekNumber());
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	/**
	 * Retrieves the {@linkplain Conversions#floatValueOf(Object) float value} {@code f}
	 * of the argument {@code o} at the current position, advances the current position
	 * and returns {@code f}.
	 *
	 * <p>If there is no argument at the current position or the argument {@code o} does not
	 * have a float value, throws a {@link BadArgumentException}. In that case, the current
	 * position is not advanced.</p>
	 *
	 * @return  the float value of the argument at current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position has no float value
	 */
	public double nextFloat() {
		return nextNumber().doubleValue();
	}

	/**
	 * Retrieves the {@linkplain Conversions#integerValueOf(Object) integer value} {@code i}
	 * of the argument {@code o} at the current position, advances the current position
	 * and returns {@code i} truncated to 32 bits (i.e., the primitive {@code int} type).
	 *
	 * <p>If there is no argument at the current position, or the argument {@code o} does not
	 * have an integer value, or the integer value does not fit in 32 bits,
	 * throws a {@link BadArgumentException}. In that case, the current position is not
	 * advanced.</p>
	 *
	 * @return  the integer value of the argument at current position cast to {@code int}
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               the argument at current position has no integer value,
	 *                               or the integer does not fit in 32 bits
	 */
	public int nextInt() {
		final int result;
		try {
			long l = Conversions.toIntegerValue(peekNumber());
			result = (int) l;
			if (l != (long) result) {
				throw new IllegalArgumentException("integer does not fit in 32 bits");
			}
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	/**
	 * Retrieves the {@linkplain Conversions#integerValueOf(Object) integer value} {@code i}
	 * of the argument {@code o} at the current position, advances the current position
	 * and returns {@code i} if {@code i >= min} and {@code i <= max}.
	 *
	 * <p>If there is no argument at the current position, or the argument {@code o} does not
	 * have an integer value, or the integer {@code i} is not within the specified range,
	 * throws a {@link BadArgumentException}. In that case, the current position is not
	 * advanced.</p>
	 *
	 * <p>The range is considered empty if {@code min > max}.</p>
	 *
	 * @param min  the minimum permitted value
	 * @param max  the maximum permitted value
	 * @param rangeName  the name of the range for error reporting, may be {@code null}
	 * @return  the integer value of the argument at current position cast to {@code int}
	 *          within the specified range
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position has no integer value,
	 *                               or the integer value is not within the specified range
	 */
	public int nextIntRange(int min, int max, String rangeName) {
		final int result;
		try {
			long value = Conversions.toIntegerValue(peekNumber());
			if (value >= min && value <= max) {
				result = (int) value;
			}
			else {
				throw new IndexOutOfBoundsException((rangeName != null ? rangeName : "value") + " out of range");
			}
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	/**
	 * Retrieves the {@linkplain Conversions#integerValueOf(Object) integer value} {@code i}
	 * of the argument {@code o} at the current position, advances the current position
	 * and returns {@code i} if {@code i >= min} and {@code i <= max}.
	 *
	 * <p>This method is equivalent to
	 * {@link #nextIntRange(int, int, String) <code>nextIntRange(min, max, null)</code>}.</p>
	 *
	 * @param min  the minimum permitted value
	 * @param max  the maximum permitted value
	 * @return  the integer value of the argument at current position cast to {@code int}
	 *          within the specified range
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position has no integer value,
	 *                               or the integer value is not within the specified range
	 */
	public int nextIntRange(int min, int max) {
		return nextIntRange(min, max, null);
	}

	/**
	 * Retrieves the {@linkplain Conversions#stringValueOf(Object) string value} {@code s}
	 * of the argument {@code o} at the current position, advances the current position
	 * and returns {@code s}.
	 *
	 * <p>If there is no argument at the current position or the argument {@code s} does not
	 * have a string value, throws a {@link BadArgumentException}. In that case, the current
	 * position is not advanced.</p>
	 *
	 * @return  the string value of the argument at current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position has no string value
	 */
	public ByteString nextString() {
		final ByteString result;
		try {
			Object arg = peek(TYPENAME_STRING.toString());
			ByteString v = Conversions.stringValueOf(arg);
			if (v != null) {
				result = v;
			}
			else {
				throw new UnexpectedArgumentException(TYPENAME_STRING.toString(), namer.typeNameOf(arg).toString());
			}
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	/**
	 * Returns the argument {@code o} at the current position if {@code o} is a string,
	 * in which case it also advances the current position.
	 *
	 * <p>If there is no argument at the current position or the argument {@code s} is not
	 * a string, throws a {@link BadArgumentException}. (In that case, the current
	 * position is not advanced.)</p>
	 *
	 * <p>This method differs from {@link #nextString()} in that this method requires
	 * {@code o} to {@linkplain net.sandius.rembulan.LuaType#isString(Object) <i>be</i> a string},
	 * wherease {@code nextString()} requires {@code o} to have
	 * a {@linkplain Conversions#stringValueOf(Object) <i>string value</i>}.</p>
	 *
	 * @return  the string value of the argument at current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position is not a string
	 */
	public ByteString nextStrictString() {
		final ByteString result;
		try {
			Object arg = peek(TYPENAME_STRING.toString());
			if (arg instanceof ByteString) {
				result = (ByteString) arg;
			}
			else if (arg instanceof String) {
				result = ByteString.of((String) arg);
			}
			else {
				throw new UnexpectedArgumentException(TYPENAME_STRING.toString(), namer.typeNameOf(arg).toString());
			}
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	private <T> T nextStrict(ByteString expectedTypeName, Class<T> clazz) {
		final T result;
		try {
			Object arg = peek(expectedTypeName.toString());
			if (arg != null && clazz.isAssignableFrom(arg.getClass())) {
				@SuppressWarnings("unchecked")
				T typed = (T) arg;
				result = typed;
			}
			else {
				throw new UnexpectedArgumentException(expectedTypeName.toString(), namer.typeNameOf(arg).toString());
			}
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	/**
	 * Returns the argument {@code o} at the current position if {@code o} is a function,
	 * in which case it also advances the current position.
	 *
	 * <p>If there is no argument at the current position or the argument {@code s} is not
	 * a function, throws a {@link BadArgumentException}. (In that case, the current
	 * position is not advanced.)</p>
	 *
	 * @return  the function at the current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position is not a function
	 */
	public LuaFunction nextFunction() {
		return nextStrict(TYPENAME_FUNCTION, LuaFunction.class);
	}

	/**
	 * Returns the argument {@code o} at the current position if {@code o} is a table,
	 * in which case it also advances the current position.
	 *
	 * <p>If there is no argument at the current position or the argument {@code s} is not
	 * a table, throws a {@link BadArgumentException}. (In that case, the current
	 * position is not advanced.)</p>
	 *
	 * @return  the table at the current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position is not a table
	 */
	public Table nextTable() {
		return nextStrict(TYPENAME_TABLE, Table.class);
	}

	/**
	 * Returns the argument {@code o} at the current position if {@code o} is a coroutine,
	 * in which case it also advances the current position.
	 *
	 * <p>If there is no argument at the current position or the argument {@code s} is not
	 * a coroutine, throws a {@link BadArgumentException}. (In that case, the current
	 * position is not advanced.)</p>
	 *
	 * @return  the coroutine at the current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position is not a coroutine
	 */
	public Coroutine nextCoroutine() {
		return nextStrict(TYPENAME_THREAD, Coroutine.class);
	}

	/**
	 * Returns the argument {@code o} at the current position if {@code o} is a (full)
	 * userdata of type {@code T}, in which case it also advances the
	 * current position.
	 *
	 * <p>If there is no argument at the current position or the argument {@code s} is not
	 * of type {@code T}, throws a {@link BadArgumentException}. (In that case, the current
	 * position is not advanced.)</p>
	 *
	 * @param typeName  type name of the userdata for error reporting, must not be {@code null}
	 * @param clazz  the class of the userdata, must not be {@code null}
	 * @return  the userdata of type {@code T} at the current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position is not a full userdata
	 *                               of type {@code T}
	 * @throws NullPointerException  if {@code typeName} or {@code clazz} is {@code null}
	 */
	public <T extends Userdata> T nextUserdata(String typeName, Class<T> clazz) {
		return nextStrict(ByteString.of(typeName), Objects.requireNonNull(clazz));
	}

	/**
	 * Returns the argument {@code o} at the current position if {@code o} is a (full)
	 * userdata, in which case it also advances the current position.
	 *
	 * <p>If there is no argument at the current position or the argument {@code s} is not
	 * a userdata, throws a {@link BadArgumentException}. (In that case, the current
	 * position is not advanced.)</p>
	 *
	 * @return  the userdata at the current position
	 *
	 * @throws BadArgumentException  if there is no argument at current position,
	 *                               or the argument at current position is not a userdata
	 */
	public Userdata nextUserdata() {
		return nextUserdata(TYPENAME_USERDATA.toString(), Userdata.class);
	}

	/**
	 * If there is an argument {@code o} at the current position, returns {@code o} and
	 * advances the current position. Otherwise, returns {@code defaultValue} (without
	 * advancing the current position).
	 *
	 * @param defaultValue  the default value, may be {@code null}
	 * @return  the argument at the current position,
	 *          or {@code defaultValue} if there is no argument at the current position
	 */
	public Object nextOptionalAny(Object defaultValue) {
		return hasNext() ? nextAny() : defaultValue;
	}

	/**
	 * If there is an argument {@code o} at the current position, returns the
	 * {@linkplain Conversions#booleanValueOf(Object) boolean value} of {@code o} and
	 * advances the current position. Otherwise, returns {@code defaultValue} (without
	 * advancing the current position).
	 *
	 * @param defaultValue  the default value
	 * @return  the boolean value of the argument at the current position,
	 *          or {@code defaultValue} if there is no argument at the current position
	 */
	public boolean nextOptionalBoolean(boolean defaultValue) {
		return hasNext() ? nextBoolean() : defaultValue;
	}

	/**
	 * If there is a non-{@code null} argument {@code o} at the current position, attempts
	 * to determine the {@link Conversions#integerValueOf(Object) integer value} of {@code o}.
	 * If {@code o} has an integer value, returns the integer value and advances the
	 * current position. If {@code o} has no integer value, throws a {@link BadArgumentException}.
	 * Otherwise (i.e., if there is no argument at the current position or if {@code o} is
	 * {@code null}), returns {@code defaultValue} without advancing the current position.
	 *
	 * @param defaultValue  the default value
	 * @return  the integer value of the argument at the current position,
	 *          or {@code defaultValue} if there is no argument at the current position
	 *          or the argument at the current position is {@code null}
	 *
	 * @throws BadArgumentException  if there is a non-{@code null} argument at the current
	 *                               position and it has no integer value
	 */
	public long nextOptionalInteger(long defaultValue) {
		return hasNext() && peek() != null ? nextInteger() : defaultValue;
	}

	/**
	 * If there is a non-{@code null} argument {@code o} at the current position, attempts
	 * to determine the {@link Conversions#floatValueOf(Object) float value} of {@code o}.
	 * If {@code o} has a float value, returns the float value and advances the
	 * current position. If {@code o} has no float value, throws a {@link BadArgumentException}.
	 * Otherwise (i.e., if there is no argument at the current position or if {@code o} is
	 * {@code null}), returns {@code defaultValue} without advancing the current position.
	 *
	 * @param defaultValue  the default value
	 * @return  the float value of the argument at the current position,
	 *          or {@code defaultValue} if there is no argument at the current position
	 *          or the argument at the current position is {@code null}
	 *
	 * @throws BadArgumentException  if there is a non-{@code null} argument at the current
	 *                               position and it has no float value
	 */
	public double nextOptionalFloat(double defaultValue) {
		return hasNext() && peek() != null ? nextFloat() : defaultValue;
	}

	/**
	 * If there is a non-{@code null} argument {@code o} at the current position, attempts
	 * to determine the {@link Conversions#integerValueOf(Object) integer value} of {@code o}.
	 * If {@code o} has an integer value that fits into a Java {@code int} type,
	 * returns this integer value as an {@code int} and advances the current position.
	 * If {@code o} has no integer value or the integer value does not fit in 32 bits,
	 * throws a {@link BadArgumentException}.
	 * Otherwise (i.e., if there is no argument at the current position or if {@code o} is
	 * {@code null}), returns {@code defaultValue} without advancing the current position.
	 *
	 * @param defaultValue  the default value
	 * @return  the integer value of the argument at the current position truncated to
	 *          a Java {@code int}, or {@code defaultValue} if there is no argument at the
	 *          current position or the argument at the current position is {@code null}
	 *
	 * @throws BadArgumentException  if there is a non-{@code null} argument at the current
	 *                               position and it has no integer value, or it has an
	 *                               integer value that does not fit into a Java {@code int}
	 */
	public int nextOptionalInt(int defaultValue) {
		return hasNext() && peek() != null ? nextInt() : defaultValue;
	}

	/**
	 * If there is a non-{@code null} argument {@code o} at the current position, attempts
	 * to determine the {@link Conversions#stringValueOf(Object) string value} of {@code o}.
	 * If {@code o} has a string value, returns the string value and advances the
	 * current position. If {@code o} has no string value, throws a {@link BadArgumentException}.
	 * Otherwise (i.e., if there is no argument at the current position or if {@code o} is
	 * {@code null}), returns {@code defaultValue} without advancing the current position.
	 *
	 * @param defaultValue  the default value, may be {@code null}
	 * @return  the string value of the argument at the current position,
	 *          or {@code defaultValue} if there is no argument at the current position
	 *          or the argument at the current position is {@code null}
	 *
	 * @throws BadArgumentException  if there is a non-{@code null} argument at the current
	 *                               position and it has no string value
	 */
	public ByteString nextOptionalString(ByteString defaultValue) {
		return hasNext() && peek() != null ? nextString() : defaultValue;
	}

	/**
	 * If there is a non-{@code null} argument {@code o} and {@code o} is a function, returns
	 * the function and advances the current position. If {@code o} is not a function,
	 * throws a {@link BadArgumentException}. Otherwise (i.e., if there is no argument at
	 * the current position or if {@code o} is {@code null}), returns {@code defaultValue}
	 * without advancing the current position.
	 *
	 * @param defaultValue  the default value, may be {@code null}
	 * @return  the function at the current position,
	 *          or {@code defaultValue} if there is no argument at the current position
	 *          or the argument at the current position is {@code null}
	 *
	 * @throws BadArgumentException  if there is a non-{@code null} argument at the current
	 *                               position and it is not a function
	 */
	public LuaFunction nextOptionalFunction(LuaFunction defaultValue) {
		return hasNext() && peek() != null ? nextFunction() : defaultValue;
	}

	/**
	 * If there is a non-{@code null} argument {@code o} and {@code o} is a table, returns
	 * the table and advances the current position. If {@code o} is not a table,
	 * throws a {@link BadArgumentException}. Otherwise (i.e., if there is no argument at
	 * the current position or if {@code o} is {@code null}), returns {@code defaultValue}
	 * without advancing the current position.
	 *
	 * @param defaultValue  the default value, may be {@code null}
	 * @return  the table at the current position,
	 *          or {@code defaultValue} if there is no argument at the current position
	 *          or the argument at the current position is {@code null}
	 *
	 * @throws BadArgumentException  if there is a non-{@code null} argument at the current
	 *                               position and it is not a table
	 */
	public Table nextOptionalTable(Table defaultValue) {
		return hasNext() && peek() != null ? nextTable() : defaultValue;
	}

	/**
	 * If there is a non-{@code null} argument {@code o} and {@code o} is a coroutine, returns
	 * the coroutine and advances the current position. If {@code o} is not a coroutine,
	 * throws a {@link BadArgumentException}. Otherwise (i.e., if there is no argument at
	 * the current position or if {@code o} is {@code null}), returns {@code defaultValue}
	 * without advancing the current position.
	 *
	 * @param defaultValue  the default value, may be {@code null}
	 * @return  the coroutine at the current position,
	 *          or {@code defaultValue} if there is no argument at the current position
	 *          or the argument at the current position is {@code null}
	 *
	 * @throws BadArgumentException  if there is a non-{@code null} argument at the current
	 *                               position and it is not a coroutine
	 */
	public Coroutine nextOptionalCoroutine(Coroutine defaultValue) {
		return hasNext() && peek() != null ? nextCoroutine() : defaultValue;
	}

}
