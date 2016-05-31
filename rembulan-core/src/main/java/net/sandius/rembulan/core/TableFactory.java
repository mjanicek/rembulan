package net.sandius.rembulan.core;

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
