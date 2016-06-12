package net.sandius.rembulan.parser.ast;

import java.util.Objects;

public final class SourceInfo {

	private final int line;
	private final int column;

	public SourceInfo(int line, int column) {
		this.line = line;
		this.column = column;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SourceInfo that = (SourceInfo) o;
		return this.line == that.line && this.column == that.column;
	}

	@Override
	public int hashCode() {
		return Objects.hash(line, column);
	}

	@Override
	public String toString() {
		return line + ":" + column;
	}

	public int line() {
		return line;
	}

	public int column() {
		return column;
	}

}
