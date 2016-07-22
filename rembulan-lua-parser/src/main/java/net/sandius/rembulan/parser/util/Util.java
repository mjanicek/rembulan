package net.sandius.rembulan.parser.util;

import java.util.Iterator;
import java.util.List;

public abstract class Util {

	private Util() {
		// not to be instantiated
	}

	public static String iterableToString(Iterable<?> collection, String sep) {
		StringBuilder bld = new StringBuilder();
		Iterator<?> it = collection.iterator();
		while (it.hasNext()) {
			bld.append(it.next());
			if (it.hasNext()) {
				bld.append(sep);
			}
		}
		return bld.toString();
	}

	public static String listToString(List<?> l, String sep) {
		return iterableToString(l, sep);
	}

}
