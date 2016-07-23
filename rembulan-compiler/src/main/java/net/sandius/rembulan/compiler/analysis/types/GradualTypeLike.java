package net.sandius.rembulan.compiler.analysis.types;

public interface GradualTypeLike<T> {

	boolean isConsistentWith(T that);

	boolean isConsistentSubtypeOf(T that);

}
