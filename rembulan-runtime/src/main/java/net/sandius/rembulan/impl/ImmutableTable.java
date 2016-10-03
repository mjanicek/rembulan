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

package net.sandius.rembulan.impl;

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.TableFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable table.
 *
 * <p>The contents of this table may be queried, but not changed: the methods
 * {@link #rawset(Object, Object)}, {@link #rawset(long, Object)} and {@link #setMetatable(Table)}
 * will throw an {@link UnsupportedOperationException}.</p>
 *
 * <p>The table has no metatable.</p>
 *
 * <p>To instantiate a new {@code ImmutableTable}, use one of the static constructor methods
 * (e.g., {@link #of(Iterable)}), or a {@link ImmutableTable.Builder} as follows:</p>
 *
 * <pre>
 *     ImmutableTable t = new ImmutableTable.Builder()
 *         .add("key1", "value1")
 *         .add("key2", "value2")
 *         .build();
 * </pre>
 *
 * <p><b>A word of caution:</b> this class violates the expectation that all Lua tables are
 * mutable, and should therefore be used with care. In order to create a mutable copy of this
 * table, use {@link #newCopy(TableFactory)}.</p>
 */
public class ImmutableTable extends Table {

	private final Map<Object, Entry> entries;
	private final Object initialKey;  // null iff the table is empty

	static class Entry {

		private final Object value;
		private final Object nextKey;  // may be null

		private Entry(Object value, Object nextKey) {
			this.value = Objects.requireNonNull(value);
			this.nextKey = nextKey;
		}

	}

	ImmutableTable(Map<Object, Entry> entries, Object initialKey) {
		this.entries = Objects.requireNonNull(entries);
		this.initialKey = initialKey;
	}

	/**
	 * Returns an {@code ImmutableTable} based on the contents of the sequence of
	 * map entries {@code entries}.
	 *
	 * <p>For every {@code key}-{@code value} pair in {@code entries}, the behaviour of this
	 * method is similar to that of {@link Table#rawset(Object, Object)}:</p>
	 * <ul>
	 *   <li>when {@code value} is <b>nil</b> (i.e., {@code null}), then {@code key}
	 *     will not have any value associated with it in the resulting table;</li>
	 *   <li>if {@code key} is <b>nil</b> or <i>NaN</i>, a {@link IllegalArgumentException}
	 *     is thrown;</li>
	 *   <li>if {@code key} is a number that has an integer value, it is converted to that integer
	 *     value.</li>
	 * </ul>
	 *
	 * <p>Keys may occur multiple times in {@code entries} &mdash; only the last occurrence
	 * counts.</p>
	 *
	 * @param entries  the map entries, must not be {@code null}
	 * @return  an immutable table based on the contents of {@code entries}
	 *
	 * @throws NullPointerException  if {@code entries} is {@code null}
	 * @throws IllegalArgumentException  if {@code entries} contains an entry with
	 *                                   a {@code null} or <i>NaN</i> key
	 */
	public static ImmutableTable of(Iterable<Map.Entry<Object, Object>> entries) {
		Builder builder = new Builder();
		for (Map.Entry<Object, Object> entry : entries) {
			builder.add(entry.getKey(), entry.getValue());
		}
		return builder.build();
	}

	/**
	 * Returns an {@code ImmutableTable} based on the contents of the map {@code map}.
	 *
	 * <p>For every {@code key}-{@code value} pair in {@code map}, the behaviour of this method
	 * is similar to that of {@link Table#rawset(Object, Object)}:</p>
	 * <ul>
	 *   <li>when {@code value} is <b>nil</b> (i.e., {@code null}), then {@code key}
	 *     will not have any value associated with it in the resulting table;</li>
	 *   <li>if {@code key} is <b>nil</b> or <i>NaN</i>, a {@link IllegalArgumentException}
	 *     is thrown;</li>
	 *   <li>if {@code key} is a number that has an integer value, it is converted to that integer
	 *     value.</li>
	 * </ul>
	 *
	 * @param map  the map used to source the contents of the table, must not be {@code null}
	 * @return  an immutable table based on the contents of {@code map}
	 *
	 * @throws NullPointerException  if {@code entries} is {@code null}
	 * @throws IllegalArgumentException  if {@code map} contains a {@code null} or <i>NaN</i> key
	 */
	public static ImmutableTable of(Map<Object, Object> map) {
		return of(map.entrySet());
	}

	/**
	 * Returns a new table constructed using the supplied {@code tableFactory}, and copies
	 * the contents of this table to it.
	 *
	 * @param tableFactory  the table factory to use, must not be {@code null}
	 * @return  a mutable copy of this table
	 */
	public Table newCopy(TableFactory tableFactory) {
		Table t = tableFactory.newTable();
		for (Object key : entries.keySet()) {
			Entry e = entries.get(key);
			t.rawset(key, e.value);
		}
		return t;
	}

	@Override
	public Object rawget(Object key) {
		key = Conversions.normaliseKey(key);
		Entry e = entries.get(key);
		return e != null ? e.value : null;
	}

	/**
	 * Throws an {@link UnsupportedOperationException}, since this table is immutable.
	 *
	 * @param key  ignored
	 * @param value  ignored
	 *
	 * @throws UnsupportedOperationException  every time this method is called
	 */
	@Override
	public void rawset(Object key, Object value) {
		throw new UnsupportedOperationException("table is immutable");
	}

	/**
	 * Throws an {@link UnsupportedOperationException}, since this table is immutable.
	 *
	 * @param idx  ignored
	 * @param value  ignored
	 *
	 * @throws UnsupportedOperationException  every time this method is called
	 */
	@Override
	public void rawset(long idx, Object value) {
		throw new UnsupportedOperationException("table is immutable");
	}

	@Override
	public Table getMetatable() {
		return null;
	}

	/**
	 * Throws an {@link UnsupportedOperationException}, since this table is immutable.
	 *
	 * @param mt  ignored
	 * @return  nothing (always throws an exception)
	 *
	 * @throws UnsupportedOperationException  every time this method is called
	 */
	@Override
	public Table setMetatable(Table mt) {
		throw new UnsupportedOperationException("table is immutable");
	}

	@Override
	public Object initialKey() {
		return initialKey;
	}

	@Override
	public Object successorKeyOf(Object key) {
		key = Conversions.normaliseKey(key);
		Entry e = entries.get(key);
		return e != null ? e.nextKey : null;
	}

	@Override
	protected void setMode(boolean weakKeys, boolean weakValues) {
		// no-op
	}

	/**
	 * Builder class for constructing instances of {@link ImmutableTable}.
	 */
	public static class Builder {

		private final Map<Object, MutableEntry> entries;
		private Object firstKey;
		private Object lastKey;

		private static class MutableEntry {

			private Object value;  // must not be null
			private Object prevKey;
			private Object nextKey;

			public MutableEntry(Object value, Object prevKey, Object nextKey) {
				this.value = value;
				this.prevKey = prevKey;
				this.nextKey = nextKey;
			}

		}

		private static void checkKey(Object key) {
			if (key == null || (key instanceof Double && Double.isNaN(((Double) key).doubleValue()))) {
				throw new IllegalArgumentException("invalid table key: " + Conversions.toHumanReadableString(key));
			}
		}

		private Builder(Map<Object, MutableEntry> entries, Object firstKey, Object lastKey) {
			this.entries = Objects.requireNonNull(entries);
			this.firstKey = firstKey;
			this.lastKey = lastKey;
		}

		/**
		 * Constructs a new empty builder.
		 */
		public Builder() {
			this(new HashMap<Object, MutableEntry>(), null, null);
		}

		private static <K, V> Map<K, V> mapCopy(Map<K, V> map) {
			Map<K, V> result = new HashMap<>();
			result.putAll(map);
			return result;
		}

		/**
		 * Constructs a copy of the given builder (a copy constructor).
		 *
		 * @param builder  the original builder, must not be {@code null}
		 *
		 * @throws  NullPointerException  if {@code builder} is {@code null}
		 */
		public Builder(Builder builder) {
			this(mapCopy(builder.entries), builder.firstKey, builder.lastKey);
		}

		/**
		 * Sets the value associated with the key {@code key} to {@code value}.
		 *
		 * <p>The behaviour of this method is similar to that of
		 * {@link Table#rawset(Object, Object)}:</p>
		 * <ul>
		 *   <li>when {@code value} is <b>nil</b> (i.e., {@code null}), the key {@code key}
		 *     will not have any value associated with it after this method returns;</li>
		 *   <li><b>nil</b> and <i>NaN</i> keys are rejected by throwing
		 *     a {@link IllegalArgumentException};</li>
		 *   <li>numeric keys with an integer value are converted to that integer value.</li>
		 * </ul>
		 *
		 * <p>The method returns {@code this}, allowing calls to this method to be chained.</p>
		 *
		 * @param key  the key, must not be {@code null} or <i>NaN</i>
		 * @param value  the value, may be {@code null}
		 * @return  this builder
		 *
		 * @throws IllegalArgumentException  when {@code key} is {@code null} or a <i>NaN</i>
		 */
		public Builder add(Object key, Object value) {
			key = Conversions.normaliseKey(key);
			checkKey(key);

			MutableEntry e = entries.get(key);

			if (e == null) {
				if (value != null) {
					// insert key at the end
					entries.put(key, new MutableEntry(value, lastKey, null));

					// update the last entry
					if (lastKey != null) {
						MutableEntry le = entries.get(lastKey);
						assert (le != null);
						assert (le.nextKey == null);
						le.nextKey = key;
					}
					lastKey = key;

					// was this the first key?
					if (firstKey == null) {
						firstKey = key;
					}
				}
				else {
					// ignore
				}
			}
			else {
				if (value != null) {
					// update value: does not influence iteration order
					e.value = value;
				}
				else {
					// remove key
					entries.remove(key);

					if (e.prevKey != null) {
						MutableEntry prev = entries.get(e.prevKey);
						prev.nextKey = e.nextKey;
					}
					else {
						// this was the first entry
						firstKey = e.nextKey;
					}

					if (e.nextKey != null) {
						MutableEntry next = entries.get(e.nextKey);
						next.prevKey = e.prevKey;
					}
					else {
						// this was the last entry
						lastKey = e.prevKey;
					}
				}
			}

			return this;
		}

		/**
		 * Clears the builder.
		 */
		public void clear() {
			entries.clear();
			firstKey = null;
			lastKey = null;
		}

		/**
		 * Constructs and returns a new immutable table based on the contents of this
		 * builder.
		 *
		 * @return  a new immutable table
		 */
		public ImmutableTable build() {
			Map<Object, Entry> tableEntries = new HashMap<>();
			for (Map.Entry<Object, MutableEntry> e : entries.entrySet()) {
				MutableEntry me = e.getValue();
				tableEntries.put(e.getKey(), new Entry(me.value, me.nextKey));
			}
			return new ImmutableTable(Collections.unmodifiableMap(tableEntries), firstKey);
		}

	}

}
