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

/**
 * A factory for {@link Table} instances.
 */
public interface TableFactory {

	/**
	 * Creates a new empty table. This is functionally equivalent to {@code newTable(0, 0)}.
	 *
	 * @return new empty table
	 * @see #newTable(int, int)
	 */
	Table newTable();

	/**
	 * Creates a new empty table with the given initial capacities for its array and hash
	 * parts.
	 *
	 * @param array  initial capacity for the array part
	 * @param hash  initial capacity for the hash part
	 * @return new empty table
	 */
	Table newTable(int array, int hash);

}
