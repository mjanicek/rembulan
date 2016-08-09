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

package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.gen.ClassNameTranslator;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;

import java.util.Comparator;

public class FunctionId {

	private final IntVector indices;

	public static final Comparator<FunctionId> LEXICOGRAPHIC_COMPARATOR = new Comparator<FunctionId>() {
		@Override
		public int compare(FunctionId a, FunctionId b) {
			int la = a.indices.length();
			int lb = b.indices.length();

			int len = Math.min(la, lb);
			for (int i = 0; i < len; i++) {
				int ai = a.indices.get(i);
				int bi = b.indices.get(i);

				int diff = ai - bi;
				if (diff != 0) {
					return diff;
				}
			}

			return la - lb;
		}
	};

	private FunctionId(IntVector indices) {
		this.indices = Check.notNull(indices);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FunctionId that = (FunctionId) o;

		return this.indices.equals(that.indices);
	}

	@Override
	public int hashCode() {
		return indices.hashCode();
	}

	private final static FunctionId ROOT = new FunctionId(IntVector.EMPTY);

	public static FunctionId root() {
		return ROOT;
	}

	public static FunctionId fromIndices(IntVector indices) {
		Check.notNull(indices);
		return indices.isEmpty() ? root() : new FunctionId(indices);
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

	public FunctionId child(int index) {
		Check.nonNegative(index);

		int[] newIndices = new int[indices.length() + 1];
		indices.copyToArray(newIndices, 0);
		newIndices[indices.length()] = index;

		return new FunctionId(IntVector.wrap(newIndices));
	}

	public FunctionId parent() {
		if (isRoot()) {
			return null;
		}
		else {
			int[] newIndices = new int[indices.length() - 1];
			indices.copyToArray(newIndices, 0, indices.length() - 1);
			return new FunctionId(IntVector.wrap(newIndices));
		}
	}

	public String toClassName(ClassNameTranslator tr) {
		Check.notNull(tr);
		for (int i = 0; i < indices.length(); i++) {
			tr = tr.child(indices.get(i));
		}
		return tr.className();
	}

}
