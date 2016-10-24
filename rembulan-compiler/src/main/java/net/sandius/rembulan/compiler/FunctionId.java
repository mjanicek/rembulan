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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class FunctionId {

	private final List<Integer> indices;

	public static final Comparator<FunctionId> LEXICOGRAPHIC_COMPARATOR = new Comparator<FunctionId>() {
		@Override
		public int compare(FunctionId a, FunctionId b) {
			int la = a.indices.size();
			int lb = b.indices.size();

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

	private FunctionId(List<Integer> indices) {
		this.indices = Objects.requireNonNull(indices);
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

	private final static FunctionId ROOT = new FunctionId(Collections.<Integer>emptyList());

	public static FunctionId root() {
		return ROOT;
	}

	public static FunctionId fromIndices(List<Integer> indices) {
		Objects.requireNonNull(indices);  // FIXME: make a copy?
		return indices.isEmpty() ? root() : new FunctionId(indices);
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		Iterator<Integer> it = indices.iterator();
		bld.append("/");
		while (it.hasNext()) {
			int i = it.next();
			bld.append(i);
			if (it.hasNext()) {
				bld.append("/");
			}
		}
		return bld.toString();
	}

	public List<Integer> indices() {
		return indices;
	}

	public boolean isRoot() {
		return indices.isEmpty();
	}

	public FunctionId child(int index) {
		Check.nonNegative(index);
		List<Integer> childIndices = new ArrayList<>(indices.size() + 1);
		childIndices.addAll(indices);
		childIndices.add(index);
		return new FunctionId(Collections.unmodifiableList(childIndices));
	}

	public FunctionId parent() {
		if (isRoot()) {
			return null;
		}
		else {
			List<Integer> subIndices = indices.subList(0, indices.size() - 1);
			return new FunctionId(subIndices);
		}
	}

	public String toClassName(ClassNameTranslator tr) {
		Objects.requireNonNull(tr);
		for (Integer index : indices) {
			tr = tr.child(index);
		}
		return tr.className();
	}

}
