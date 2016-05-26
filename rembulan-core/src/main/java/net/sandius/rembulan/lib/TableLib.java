package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;

/**
 * <p>This library provides generic functions for table manipulation. It provides all its functions
 * inside the table {@code table}.</p>
 *
 * Remember that, whenever an operation needs the length of a table, the table must be a proper
 * sequence or have a {@code __len} metamethod (see §3.4.7 of the Lua Reference Manual).
 * All functions ignore non-numeric keys in the tables given as arguments.
 */
public abstract class TableLib implements Lib {

	@Override
	public void installInto(LuaState state, Table env) {
		LibUtils.setIfNonNull(env, "concat", _concat());
		LibUtils.setIfNonNull(env, "insert", _insert());
		LibUtils.setIfNonNull(env, "move", _move());
		LibUtils.setIfNonNull(env, "pack", _pack());
		LibUtils.setIfNonNull(env, "remove", _remove());
		LibUtils.setIfNonNull(env, "sort", _sort());
		LibUtils.setIfNonNull(env, "unpack", _unpack());
	}

	/**
	 * {@code table.concat (list [, sep [, i [, j]]])}
	 *
	 * <p>Given a list where all elements are strings or numbers, returns the string
	 * {@code list[i]..sep..list[i+1] ··· sep..list[j]}. The default value for {@code sep}
	 * is the empty string, the default for {@code i} is 1, and the default for {@code j}
	 * is {@code #list}. If {@code i} is greater than {@code j}, returns the empty string.</p>
	 */
	public abstract Function _concat();

	/**
	 * {@code table.insert (list, [pos,] value)}
	 *
	 * <p>Inserts element value at position {@code pos} in {@code list}, shifting up the elements
	 * {@code list[pos], list[pos+1], ···, list[#list]}. The default value for {@code pos}
	 * is {@code #list+1}, so that a call {@code table.insert(t,x)} inserts {@code x}
	 * at the end of list {@code t}.</p>
	 */
	public abstract Function _insert();

	/**
	 * {@code table.move (a1, f, e, t [,a2])}
	 *
	 * <p>Moves elements from table {@code a1} to table {@code a2}. This function performs
	 * the equivalent to the following multiple assignment:
	 * {@code a2[t],··· = a1[f],···,a1[e]}. The default for {@code a2} is {@code a1}.
	 * The destination range can overlap with the source range. The number of elements
	 * to be moved must fit in a Lua integer.</p>
	 */
	public abstract Function _move();

	/**
	 * {@code table.pack (···)}
	 *
	 * <p>Returns a new table with all parameters stored into keys 1, 2, etc. and with
	 * a field {@code "n"} with the total number of parameters. Note that the resulting table
	 * may not be a sequence.</p>
	 */
	public abstract Function _pack();

	/**
	 * {@code table.remove (list [, pos])}
	 *
	 * <p>Removes from {@code list} the element at position {@code pos}, returning the value
	 * of the removed element. When {@code pos} is an integer between 1 and {@code #list},
	 * it shifts down the elements{@code  list[pos+1], list[pos+2], ···, list[#list]}
	 * and erases element {@code list[#list]}; The index pos can also be 0 when {@code #list}
	 * is 0, or {@code #list + 1}; in those cases, the function erases the element
	 * {@code list[pos]}.</p>
	 *
	 * <p>The default value for {@code pos} is {@code #list}, so that a call
	 * {@code table.remove(l)} removes the last element of list {@code l}.</p>
	 */
	public abstract Function _remove();

	/**
	 * {@code table.sort (list [, comp])}
	 *
	 * <p>Sorts list elements in a given order, in-place, from {@code list[1]}
	 * to {@code list[#list]}. If {@code comp} is given, then it must be a function that receives
	 * two list elements and returns <b>true</b> when the first element must come before
	 * the second in the final order (so that, after the sort, {@code i < j} implies
	 * {@code not comp(list[j],list[i])}). If {@code comp} is not given, then the standard
	 * Lua operator {@code <} is used instead.</p>
	 *
	 * <p>Note that the {@code comp} function must define a strict partial order over the elements
	 * in the list; that is, it must be asymmetric and transitive. Otherwise, no valid sort may
	 * be possible.</p>
	 *
	 * <p>The sort algorithm is not stable; that is, elements not comparable by the given order
	 * (e.g., equal elements) may have their relative positions changed by the sort.</p>
	 */
	public abstract Function _sort();

	/**
	 * {@code table.unpack (list [, i [, j]])}
	 *
	 * <p>Returns the elements from the given list. This function is equivalent to
	 * <blockquote>
	 *  {@code return list[i], list[i+1], ···, list[j] }
	 * </blockquote>
	 * By default, {@code i} is 1 and {@code j} is {@code #list}.</p>
	 */
	public abstract Function _unpack();

}
