package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;

public class PrototypePath {

	private final IntVector indices;

	private PrototypePath(IntVector indices) {
		this.indices = Check.notNull(indices);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PrototypePath that = (PrototypePath) o;

		return this.indices.equals(that.indices);
	}

	@Override
	public int hashCode() {
		return indices.hashCode();
	}

	private final static PrototypePath ROOT = new PrototypePath(IntVector.EMPTY);

	public static PrototypePath root() {
		return ROOT;
	}

	public static PrototypePath fromIndices(IntVector indices) {
		Check.notNull(indices);
		return indices.isEmpty() ? root() : new PrototypePath(indices);
	}

	@Override
	public String toString() {
		return "/" + indices.toString("/");
	}

	public IntVector indices() {
		return indices;
	}

	public boolean isRoot() {
		return indices.isEmpty();
	}

	public PrototypePath child(int index) {
		Check.nonNegative(index);

		int[] newIndices = new int[indices.length() + 1];
		indices.copyToArray(newIndices, 0);
		newIndices[indices.length()] = index;

		return new PrototypePath(IntVector.wrap(newIndices));
	}

}
