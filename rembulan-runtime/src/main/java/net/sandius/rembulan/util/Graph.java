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

package net.sandius.rembulan.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Graph<T> {

	protected final Set<T> vertices;
	protected final Set<Pair<T, T>> edges;

	private Graph(Set<T> vertices, Set<Pair<T, T>> edges) {
		this.vertices = Check.notNull(vertices);
		this.edges = Check.notNull(edges);
	}

	public static <T> Graph<T> wrap(Set<T> vertices, Set<Pair<T, T>> edges) {
		return new Graph<T>(vertices, edges);
	}

	public static <T> Graph<T> immutableCopyFrom(Iterable<T> vertices, Iterable<Pair<T, T>> edges) {
		Set<T> vs = new HashSet<>();
		Set<Pair<T, T>> es = new HashSet<>();

		for (T v : vertices) {
			vs.add(v);
		}

		for (Pair<T, T> e : edges) {
			es.add(e);
		}

		return wrap(Collections.unmodifiableSet(vs), Collections.unmodifiableSet(es));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Graph<?> graph = (Graph<?>) o;

		return vertices.equals(graph.vertices) && edges.equals(graph.edges);
	}

	@Override
	public int hashCode() {
		int result = vertices.hashCode();
		result = 31 * result + edges.hashCode();
		return result;
	}

	public Set<T> vertices() {
		return vertices;
	}

	public Set<Pair<T, T>> edges() {
		return edges;
	}

}
