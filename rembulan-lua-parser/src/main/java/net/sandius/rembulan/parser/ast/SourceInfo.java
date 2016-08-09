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
