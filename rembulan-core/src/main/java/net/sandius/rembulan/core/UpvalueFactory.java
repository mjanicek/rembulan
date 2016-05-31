package net.sandius.rembulan.core;

/**
 * A factory for creating {@link Upvalue} instances.
 */
public interface UpvalueFactory {

	/**
	 * Creates a new Upvalue with the given initial value.
	 *
	 * @param initialValue  initial value, may be null
	 * @return new instance of Upvalue with the given value
	 */
	Upvalue newUpvalue(Object initialValue);

}
