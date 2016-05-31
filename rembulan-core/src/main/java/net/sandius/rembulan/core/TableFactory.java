package net.sandius.rembulan.core;

public interface TableFactory {

	// equivalent to newTable(0, 0)
	Table newTable();

	Table newTable(int array, int hash);

}
