package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.parser.util.Util;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.UnmodifiableIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Blocks {

	private final List<BasicBlock> blocks;

	private Blocks(List<BasicBlock> blocks) {
		verify(blocks);
		this.blocks = Check.notNull(blocks);
	}

	public static Blocks of(List<BasicBlock> blocks) {
		return new Blocks(
				new ArrayList<>(Check.notNull(blocks)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Blocks that = (Blocks) o;
		return this.blocks.equals(that.blocks);
	}

	@Override
	public int hashCode() {
		return blocks.hashCode();
	}

	private static List<BasicBlock> verify(List<BasicBlock> blocks) {
		Check.notNull(blocks);
		if (blocks.isEmpty()) {
			throw new IllegalArgumentException("Empty block sequence");
		}
		Set<Label> defs = new HashSet<>();
		Set<Label> pending = new HashSet<>();
		for (BasicBlock b : blocks) {
			Label l = b.label();
			if (!defs.add(l)) {
				throw new IllegalArgumentException("Label " + l + " defined more than once");
			}
			else {
				pending.remove(l);
			}

			for (Label nxt : b.end().nextLabels()) {
				if (!defs.contains(nxt)) {
					pending.add(nxt);
				}
			}
		}

		if (!pending.isEmpty()) {
			throw new IllegalStateException("Label(s) not defined: " + Util.iterableToString(pending, ", "));
		}

		return blocks;
	}

	// index for hashmap-based lookup
	public Map<Label, BasicBlock> index() {
		Map<Label, BasicBlock> result = new HashMap<>();
		for (BasicBlock b : blocks) {
			result.put(b.label(), b);
		}
		return result;
	}

	public Label entryLabel() {
		return blocks.get(0).label();
	}

	public Iterator<BasicBlock> blockIterator() {
		return new UnmodifiableIterator<>(blocks.iterator());
	}

}
