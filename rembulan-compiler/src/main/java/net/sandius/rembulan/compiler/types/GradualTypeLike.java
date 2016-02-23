package net.sandius.rembulan.compiler.types;

public interface GradualTypeLike<T> {

	boolean isConsistentWith(T that);

	boolean isConsistentSubtypeOf(T that);

}
