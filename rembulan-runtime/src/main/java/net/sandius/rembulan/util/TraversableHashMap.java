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

package net.sandius.rembulan.util;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * A hashmap similar in functionality to {@link java.util.LinkedHashMap}, with the additional
 * accessor methods for successor and predecessor keys.
 *
 * <p>To get the keys following and preceding {@code k} in the map, use
 * {@link #getSuccessorOf(Object)} and {@link #getPredecessorOf(Object)}, respectively.
 * In order to access the first and last keys in the traversal orders, use {@link #getFirstKey()}
 * and {@link #getLastKey()}.</p>
 *
 * @param <K>  key type
 * @param <V>  value type
 */
public class TraversableHashMap<K, V> implements Map<K, V> {

	private final HashMap<K, Entry<K, V>> entries;
	private K firstKey;
	private K lastKey;

	private final Set<K> keySet;
	private final Collection<V> values;
	private final Set<Map.Entry<K, V>> entrySet;

	/**
	 * Constructs a new empty map.
	 */
	public TraversableHashMap() {
		this.entries = new HashMap<>();
		this.firstKey = null;
		this.lastKey = null;

		this.keySet = new KeySet();
		this.values = new Values();
		this.entrySet = new EntrySet();
	}

	static class Entry<K, V> {

		private V value;  // must not be null
		private K prevKey;  // may be null
		private K nextKey;  // may be null

		public Entry(V value, K prevKey, K nextKey) {
			this.value = Objects.requireNonNull(value);
			this.prevKey = prevKey;
			this.nextKey = nextKey;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V newValue) {
			V oldValue = value;
			this.value = Objects.requireNonNull(newValue);
			return oldValue;
		}

		public K getPreviousKey() {
			return prevKey;
		}

		public void setPreviousKey(K key) {
			this.prevKey = key;
		}

		public K getNextKey() {
			return nextKey;
		}

		public void setNextKey(K key) {
			this.nextKey = key;
		}

	}

	@Override
	public int size() {
		return entries.size();
	}

	@Override
	public boolean isEmpty() {
		return entries.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return entries.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		if (value == null) {
			return false;
		}

		for (Entry<K, V> e : entries.values()) {
			if (value.equals(e.getValue())) return true;
		}

		return false;
	}

	@Override
	public V get(Object key) {
		Entry<K, V> e = entries.get(key);
		return e != null ? e.getValue() : null;
	}

	@Override
	public V put(K key, V value) {
		Objects.requireNonNull(key, "key is null");
		Objects.requireNonNull(value, "value is null");

		Entry<K, V> e = entries.get(key);

		if (e == null) {
			// insert key at the end
			entries.put(key, new Entry<>(value, lastKey, null));

			// update the last entry
			if (lastKey != null) {
				entries.get(lastKey).setNextKey(key);
			}
			lastKey = key;

			// was this the first key?
			if (firstKey == null) {
				firstKey = key;
			}

			return null;
		}
		else {
			// update value: does not influence iteration order
			return e.setValue(value);
		}
	}

	@Override
	public V remove(Object key) {
		Objects.requireNonNull(key, "key is null");

		// remove key
		Entry<K, V> e = entries.remove(key);

		if (e != null) {

			K prevKey = e.getPreviousKey();
			K nextKey = e.getNextKey();

			if (prevKey != null) {
				entries.get(prevKey).setNextKey(nextKey);
			}
			else {
				// this was the first entry
				firstKey = nextKey;
			}

			if (nextKey != null) {
				entries.get(nextKey).setPreviousKey(prevKey);
			}
			else {
				// this was the last entry
				lastKey = prevKey;
			}

			return e.getValue();
		}
		else {
			return null;
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			this.put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		entries.clear();
		firstKey = null;
		lastKey = null;
	}

	/**
	 * Returns the first key in the traversal order.
	 *
	 * @return  the first key, or {@code null} if the map is empty
	 */
	public K getFirstKey() {
		return firstKey;
	}

	/**
	 * Returns the last key in the traversal order.
	 *
	 * @return  the last key, or {@code null} if the map is empty
	 */
	public K getLastKey() {
		return lastKey;
	}

	/**
	 * Returns the key following {@code key} in the traversal order, or {@code null} if
	 * {@code key} is the last key in the traversal order.
	 *
	 * @param key  the key to find the successor of, must not be {@code null}
	 * @return  the key following {@code key}, or {@code null} if {@code key} is the last key
	 *
	 * @throws NullPointerException  if {@code key} is {@code null}
	 * @throws NoSuchElementException  if the map does not associate any value to {@code key}
	 */
	public K getSuccessorOf(K key) {
		Objects.requireNonNull(key);
		Entry<K, V> e = entries.get(key);
		if (e == null) {
			throw new NoSuchElementException(key.toString());
		}
		return e.getNextKey();
	}

	/**
	 * Returns the key preceding {@code key} in the traversal order, or {@code null} if
	 * {@code key} is the first key in the traversal order.
	 *
	 * @param key  the key to find the predecessor of, must not be {@code null}
	 * @return  the key preceding {@code key}, or {@code null} if {@code key} is the last key
	 *
	 * @throws NullPointerException  if {@code key} is {@code null}
	 * @throws NoSuchElementException  if the map does not associate any value to {@code key}
	 */
	public K getPredecessorOf(K key) {
		Objects.requireNonNull(key);
		Entry<K, V> e = entries.get(key);
		if (e == null) {
			throw new NoSuchElementException(key.toString());
		}
		return e.getPreviousKey();
	}

	private abstract class AbstractEntryIterator<T> implements Iterator<T> {

		private K key;
		private boolean nextCalled;

		AbstractEntryIterator() {
			this.key = firstKey;
			this.nextCalled = false;
		}

		@Override
		public boolean hasNext() {
			return key != null;
		}

		protected abstract T get(K k, Entry<K, V> v);

		@Override
		public T next() {
			K k = key;
			Entry<K, V> e = entries.get(k);
			if (e == null) {
				throw new NoSuchElementException(Objects.toString(key));
			}

			nextCalled = true;
			key = e.getNextKey();
			return get(k, e);
		}

		@Override
		public void remove() {
			Entry<K, V> e = entries.remove(key);
			if (!nextCalled) {
				throw new IllegalStateException();
			}
			nextCalled = false;
		}

	}

	private class KeyIterator extends AbstractEntryIterator<K> {

		@Override
		protected K get(K k, Entry<K, V> v) {
			return k;
		}

	}

	private class KeySet extends AbstractSet<K> {

		@Override
		public Iterator<K> iterator() {
			return new KeyIterator();
		}

		@Override
		public int size() {
			return TraversableHashMap.this.size();
		}

		@Override
		public void clear() {
			TraversableHashMap.this.clear();
		}
	}

	@Override
	public Set<K> keySet() {
		return keySet;
	}

	private class ValueIterator extends AbstractEntryIterator<V> {

		@Override
		protected V get(K k, Entry<K, V> v) {
			return v.getValue();
		}

	}

	private class Values extends AbstractCollection<V> {

		@Override
		public Iterator<V> iterator() {
			return new ValueIterator();
		}

		@Override
		public int size() {
			return TraversableHashMap.this.size();
		}

		@Override
		public void clear() {
			TraversableHashMap.this.clear();
		}

	}

	@Override
	public Collection<V> values() {
		return values;
	}

	private static class MapEntryAdapter<K, V> implements Map.Entry<K, V> {

		private final K key;
		private final Entry<K, V> entry;

		MapEntryAdapter(K key, Entry<K, V> entry) {
			this.key = Objects.requireNonNull(key);
			this.entry = Objects.requireNonNull(entry);
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return entry.getValue();
		}

		@Override
		public V setValue(V value) {
			return entry.setValue(value);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			Map.Entry<?, ?> that = (Map.Entry<?, ?>) o;
			Object thatKey = that.getKey();
			Object thatValue = that.getValue();

			return !(thatKey == null || thatValue == null)
					&& (key.equals(thatKey) && entry.getValue().equals(thatValue));
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(key) ^ Objects.hashCode(entry.getValue());
		}
	}

	private class MapEntryIterator extends AbstractEntryIterator<Map.Entry<K, V>> {

		@Override
		protected Map.Entry<K, V> get(K k, Entry<K, V> v) {
			return new MapEntryAdapter<>(k, v);
		}

	}

	private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new MapEntryIterator();
		}

		@Override
		public int size() {
			return TraversableHashMap.this.size();
		}

		@Override
		public void clear() {
			TraversableHashMap.this.clear();
		}

	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return entrySet;
	}

}
