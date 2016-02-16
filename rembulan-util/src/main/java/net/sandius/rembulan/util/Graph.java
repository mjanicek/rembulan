package net.sandius.rembulan.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Graph<T> {

	protected final Set<T> vertices;
	protected final Set<Pair<T, T>> edges;

	public Graph(Set<T> vertices, Set<Pair<T, T>> edges) {
		this.vertices = Objects.requireNonNull(vertices);
		this.edges = Objects.requireNonNull(edges);
	}

	public static <T> Graph<T> copyFrom(Iterable<T> vertices, Iterable<Pair<T, T>> edges) {
		Set<T> vs = new HashSet<>();
		Set<Pair<T, T>> es = new HashSet<>();

		for (T v : vertices) {
			vs.add(v);
		}

		for (Pair<T, T> e : edges) {
			es.add(e);
		}

		return new Graph<T>(vs, es);
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
