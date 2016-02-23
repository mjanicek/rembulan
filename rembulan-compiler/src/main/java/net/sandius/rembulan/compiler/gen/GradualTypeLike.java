package net.sandius.rembulan.compiler.gen;

public interface GradualTypeLike<T> {

	boolean isConsistentWith(T that);

	boolean isConsistentSubtypeOf(T that);

}
