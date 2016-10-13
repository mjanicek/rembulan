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

import java.util.Objects;

/**
 * Metatable keys and utilities.
 */
public final class Metatables {

	private Metatables() {
		// not to be instantiated
	}

	/**
	 * The metatable key {@code "__add"}. When defined, customises the behaviour of
	 * the Lua addition operator ({@code +}).
	 */
	public static final ByteString MT_ADD = ByteString.of("__add");

	/**
	 * The metatable key {@code "__sub"}. When defined, customises the behaviour of
	 * the Lua subtraction operator (binary {@code -}).
	 */
	public static final ByteString MT_SUB = ByteString.of("__sub");

	/**
	 * The metatable key {@code "__mul"}. When defined, customises the behaviour of
	 * the Lua multiplication operator ({@code *}).
	 */
	public static final ByteString MT_MUL = ByteString.of("__mul");

	/**
	 * The metatable key {@code "__div"}. When defined, customises the behaviour of
	 * the Lua division operator ({@code /}).
	 */
	public static final ByteString MT_DIV = ByteString.of("__div");

	/**
	 * The metatable key {@code "__mod"}. When defined, customises the behaviour of
	 * the Lua modulo operator ({@code %}).
	 */
	public static final ByteString MT_MOD = ByteString.of("__mod");

	/**
	 * The metatable key {@code "__pow"}. When defined, customises the behaviour of
	 * the Lua exponentiation operator ({@code ^}).
	 */
	public static final ByteString MT_POW = ByteString.of("__pow");

	/**
	 * The metatable key {@code "__unm"}. When defined, customises the behaviour of
	 * the Lua unary minus operator (unary {@code -}).
	 */
	public static final ByteString MT_UNM = ByteString.of("__unm");

	/**
	 * The metatable key {@code "__idiv"}. When defined, customises the behaviour of
	 * the Lua floor division ({@code //}).
	 */
	public static final ByteString MT_IDIV = ByteString.of("__idiv");

	/**
	 * The metatable key {@code "__band"}. When defined, customises the behaviour of
	 * the Lua bitwise AND operator ({@code &}).
	 */
	public static final ByteString MT_BAND = ByteString.of("__band");

	/**
	 * The metatable key {@code "__bor"}. When defined, customises the behaviour of
	 * the Lua bitwise OR operator ({@code |}).
	 */
	public static final ByteString MT_BOR = ByteString.of("__bor");

	/**
	 * The metatable key {@code "__bxor"}. When defined, customises the behaviour of
	 * the Lua bitwise XOR operator (binary {@code ~}).
	 */
	public static final ByteString MT_BXOR = ByteString.of("__bxor");

	/**
	 * The metatable key {@code "__bnot"}. When defined, customises the behaviour of
	 * the Lua bitwise NOT operator (unary {@code ~}).
	 */
	public static final ByteString MT_BNOT = ByteString.of("__bnot");

	/**
	 * The metatable key {@code "__shl"}. When defined, customises the behaviour of
	 * the Lua bitwise left shift operator ({@code <<}).
	 */
	public static final ByteString MT_SHL = ByteString.of("__shl");

	/**
	 * The metatable key {@code "__shr"}. When defined, customises the behaviour of
	 * the Lua bitwise right shift operator ({@code >>}).
	 */
	public static final ByteString MT_SHR = ByteString.of("__shr");

	/**
	 * The metatable key {@code "__concat"}. When defined, customises the behaviour of
	 * the Lua concatenation operator ({@code ..}).
	 */
	public static final ByteString MT_CONCAT = ByteString.of("__concat");

	/**
	 * The metatable key {@code "__len"}. When defined, customises the behaviour of
	 * the Lua length operator ({@code #}).
	 */
	public static final ByteString MT_LEN = ByteString.of("__len");

	/**
	 * The metatable key {@code "__eq"}. When defined, customises the behaviour of
	 * the Lua equality operator ({@code ==}).
	 */
	public static final ByteString MT_EQ = ByteString.of("__eq");

	/**
	 * The metatable key {@code "__lt"}. When defined, customises the behaviour of
	 * the Lua lesser-than operator ({@code <}).
	 */
	public static final ByteString MT_LT = ByteString.of("__lt");

	/**
	 * The metatable key {@code "__le"}. When defined, customises the behaviour of
	 * the Lua lesser-than-or-equal-to operator ({@code <=}).
	 */
	public static final ByteString MT_LE = ByteString.of("__le");

	/**
	 * The metatable key {@code "__index"}. When defined, customises the behaviour of
	 * the (non-assignment) Lua table access operator ({@code t[k]}).
	 */
	public static final ByteString MT_INDEX = ByteString.of("__index");

	/**
	 * The metatable key {@code "__newindex"}. When defined, customises the behaviour of
	 * Lua table assignment ({@code t[k] = v}).
	 */
	public static final ByteString MT_NEWINDEX = ByteString.of("__newindex");

	/**
	 * The metatable key {@code "__call"}. When defined, customises the behaviour of
	 * the Lua call operator ({@code f(args)}).
	 */
	public static final ByteString MT_CALL = ByteString.of("__call");

	/**
	 * The metatable key {@code "__mode"}. Used to control the weakness of table keys
	 * and values.
	 */
	public static final ByteString MT_MODE = ByteString.of("__mode");

	/**
	 * Returns the entry with the key {@code event} of the metatable of the {@link LuaObject}
	 * {@code o}. If {@code o} does not have a metatable or {@code event} does not exist in it as
	 * a key, returns {@code null}.
	 *
	 * <p>The access of the metatable is raw (i.e. uses {@link Table#rawget(Object)}).</p>
	 *
	 * <p>This method differs from {@link #getMetamethod(MetatableProvider, ByteString, Object)}
	 * in that it does not require a metatable provider as the object in question is known
	 * to have metatables attached on a per-instance basis.</p>
	 *
	 * @param event  the key to look up in the metatable, must not be {@code null}
	 * @param o  the object in question, must not be {@code null}
	 * @return a non-{@code null} value if {@code event} is a key in {@code o}'s metatable;
	 *         {@code null} otherwise
	 *
	 * @throws NullPointerException  if {@code o} or {@code event} is {@code null}
	 */
	public static Object getMetamethod(ByteString event, LuaObject o) {
		Objects.requireNonNull(event);
		Objects.requireNonNull(o);

		Table mt = o.getMetatable();
		if (mt != null) {
			return mt.rawget(event);
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the entry with the key {@code event} of the metatable of the object {@code o}.
	 * If {@code o} does not have a metatable or {@code event} does not exist in it as
	 * a key, returns {@code null}.
	 *
	 * <p>The access of the metatable is raw (i.e. uses {@link Table#rawget(Object)}).</p>
	 *
	 * @param metatableProvider  the metatable provider, must not be {@code null}
	 * @param event  the key to look up in the metatable, must not be {@code null}
	 * @param o  the object in question, may be {@code null}
	 * @return  a non-{@code null} value if {@code event} is a key in {@code o}'s metatable;
	 *          {@code null} otherwise
	 *
	 * @throws NullPointerException  if {@code metatableProvider} or {@code event} is {@code null}
	 */
	public static Object getMetamethod(MetatableProvider metatableProvider, ByteString event, Object o) {
		Objects.requireNonNull(event);
		// o can be null

		Table mt = metatableProvider.getMetatable(o);
		if (mt != null) {
			return mt.rawget(event);
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the metatable entry {@code event} for {@code a} or in {@code b}, or {@code null}
	 * if neither {@code a} nor {@code b} has such an entry in their metatable.
	 *
	 * <p>This method is similar to {@link #getMetamethod(MetatableProvider, ByteString, Object)},
	 * but first looks up the entry {@code event} in {@code a}, and if this fails (by
	 * returning {@code null}), tries to look {@code event} up in {@code b}.
	 *
	 * @param metatableProvider  the metatable provider, must not be {@code null}
	 * @param event  the key to look up in the metatable, must not be {@code null}
	 * @param a  the first object to try, may be {@code null}
	 * @param b  the second object to try, may be {@code null}
	 *
	 * @return  a non-{@code null} value if {@code event} is a key in {@code a}'s or {@code b}'s
	 *          metatable (in this order); {@code null} otherwise
	 *
	 * @throws NullPointerException  if {@code metatableProvider} or {@code event} is {@code null}
	 */
	public static Object binaryHandlerFor(MetatableProvider metatableProvider, ByteString event, Object a, Object b) {
		Objects.requireNonNull(metatableProvider);
		Objects.requireNonNull(event);
		Object ma = Metatables.getMetamethod(metatableProvider, event, a);
		return ma != null ? ma : Metatables.getMetamethod(metatableProvider, event, b);
	}

}
