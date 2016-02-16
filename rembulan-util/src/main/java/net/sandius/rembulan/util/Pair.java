package net.sandius.rembulan.util;

public final class Pair<T, U> {

	public final T first;
	public final U second;

	public Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Pair<?, ?> pair = (Pair<?, ?>) o;

		return !(first != null ? !first.equals(pair.first) : pair.first != null)
				&& !(second != null ? !second.equals(pair.second) : pair.second != null);
	}

	@Override
	public int hashCode() {
		int result = first != null ? first.hashCode() : 0;
		result = 31 * result + (second != null ? second.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "(" + (first != null ? first.toString() : "null")
				+ "," + (second != null ? second.toString() : "null") + ")";
	}

	public T first() {
		return first;
	}

	public U second() {
		return second;
	}

}
