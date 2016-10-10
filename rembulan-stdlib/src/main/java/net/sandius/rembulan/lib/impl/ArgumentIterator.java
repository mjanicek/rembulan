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

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Userdata;
import net.sandius.rembulan.ValueTypeNamer;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.UnexpectedArgumentException;
import net.sandius.rembulan.runtime.Coroutine;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.util.Check;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static net.sandius.rembulan.LuaFormat.TYPENAME_FUNCTION;
import static net.sandius.rembulan.LuaFormat.TYPENAME_NUMBER;
import static net.sandius.rembulan.LuaFormat.TYPENAME_STRING;
import static net.sandius.rembulan.LuaFormat.TYPENAME_TABLE;
import static net.sandius.rembulan.LuaFormat.TYPENAME_THREAD;
import static net.sandius.rembulan.LuaFormat.TYPENAME_USERDATA;

public class ArgumentIterator implements Iterator<Object> {

	private final ValueTypeNamer namer;

	private final String name;
	private final Object[] args;
	private int index;

	private ArgumentIterator(ValueTypeNamer namer, String name, Object[] args, int index) {
		this.namer = Check.notNull(namer);
		this.name = Check.notNull(name);
		this.args = Check.notNull(args);
		this.index = Check.nonNegative(index);
	}

	public ArgumentIterator(ValueTypeNamer namer, String name, Object[] args) {
		this(namer, name, args, 0);
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

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public ValueTypeNamer namer() {
		return namer;
	}

	public int at() {
		// 0-based!
		return index;
	}

	public void skip() {
		index += 1;
	}

	public void goTo(int index) {
		this.index = index;
	}

	public void rewind() {
		goTo(0);
	}

	public int size() {
		return args.length;
	}

	public int tailSize() {
		return Math.max(args.length - index, 0);
	}

	public Object[] getAll() {
		return args;
	}

	public Object[] getTail() {
		return Arrays.copyOfRange(args, index, args.length);
	}

	protected BadArgumentException badArgument(Throwable cause) {
		return new BadArgumentException(index + 1, name, cause);
	}

	protected BadArgumentException badArgument(int argIndex, String message) {
		return new BadArgumentException(argIndex, name, message);
	}

	protected BadArgumentException badArgument(String message) {
		return new BadArgumentException(index + 1, name, message);
	}

	protected Object peek() {
		if (index < args.length) {
			return args[index];
		}
		else {
			throw new NoSuchElementException("value expected");
		}
	}

	protected Object peek(String expectedType) {
		try {
			return peek();
		}
		catch (NoSuchElementException ex) {
			throw new UnexpectedArgumentException(expectedType, "no value");
		}
	}

	// guaranteed not to return null
	protected Number peekNumber() {
		Object arg = peek(TYPENAME_NUMBER);
		Number n = Conversions.numericalValueOf(arg);
		if (n != null) {
			return n;
		}
		else {
			throw new UnexpectedArgumentException(TYPENAME_NUMBER, namer.typeNameOf(arg));
		}
	}

	public Object peekOrNil() {
		if (hasNext()) {
			return peek();
		}
		else {
			return null;
		}
	}

	public <T> T nextStrict(String expectedTypeName, Class<T> clazz) {
		final T result;
		try {
			Object arg = peek(expectedTypeName);
			if (arg != null && clazz.isAssignableFrom(arg.getClass())) {
				@SuppressWarnings("unchecked")
				T typed = (T) arg;
				result = typed;
			}
			else {
				throw new UnexpectedArgumentException(expectedTypeName, namer.typeNameOf(arg));
			}
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	public <T> T nextStrictOrNil(String expectedTypeName, Class<T> clazz) {
		final T result;
		try {
			Object arg = peek(expectedTypeName);
			if (arg == null || clazz.isAssignableFrom(arg.getClass())) {
				@SuppressWarnings("unchecked")
				T typed = (T) arg;
				result = typed;
			}
			else {
				throw new UnexpectedArgumentException(expectedTypeName, namer.typeNameOf(arg));
			}
		}
		catch (RuntimeException ex) {
			String message = "nil or " + expectedTypeName + " expected";
			throw badArgument(message);
		}
		skip();
		return result;
	}

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

	// does not distinguish missing value vs nil
	// this should be the last argument to check, otherwise use peekOrNil() followed by skip()
	public Object optNextAny() {
		if (hasNext()) {
			return nextAny();
		}
		else {
			return null;
		}
	}

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

	public double nextFloat() {
		return nextNumber().doubleValue();
	}

	public int nextInt() {
		return (int) nextInteger();
	}

	public int optNextInt(int defaultValue) {
		if (hasNext()) {
			Object o = peek();
			if (o == null) {
				skip();
				return defaultValue;
			}
			else {
				return nextInt();
			}
		}
		else {
			return defaultValue;
		}
	}

	public int nextIntRange(String rangeName, int min, int max) {
		final int result;
		try {
			long value = Conversions.toIntegerValue(peekNumber());
			if (value >= min && value <= max) {
				result = (int) value;
			}
			else {
				throw new IndexOutOfBoundsException(rangeName + " out of range");
			}
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	public boolean optNextBoolean(boolean defaultValue) {
		if (hasNext()) {
			Object o = peek();
			boolean result = Conversions.booleanValueOf(o);
			skip();
			return result;
		}
		else {
			return defaultValue;
		}
	}

	// FIXME: use ByteString
	public String nextString() {
		final String result;
		try {
			Object arg = peek(TYPENAME_STRING);
			ByteString v = Conversions.stringValueOf(arg);
			if (v != null) {
				result = v.toString();
			}
			else {
				throw new UnexpectedArgumentException(TYPENAME_STRING, namer.typeNameOf(arg));
			}
		}
		catch (RuntimeException ex) {
			throw badArgument(ex);
		}
		skip();
		return result;
	}

	public String nextStrictString() {
		return nextStrict(TYPENAME_STRING, String.class);
	}

	public String optNextString(String defaultValue) {
		return hasNext() ? nextString() : defaultValue;
	}

	public LuaFunction nextFunction() {
		return nextStrict(TYPENAME_FUNCTION, LuaFunction.class);
	}

	public Table nextTable() {
		return nextStrict(TYPENAME_TABLE, Table.class);
	}

	public Table nextTableOrNil() {
		return nextStrictOrNil(TYPENAME_TABLE, Table.class);
	}

	public Coroutine nextCoroutine() {
		return nextStrict(TYPENAME_THREAD, Coroutine.class);
	}

	public <T extends Userdata> T nextUserdata(String typeName, Class<T> clazz) {
		return nextStrict(typeName, clazz);
	}

	public Userdata nextUserdata() {
		return nextUserdata(TYPENAME_USERDATA, Userdata.class);
	}

}
