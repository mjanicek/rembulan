package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.UnmodifiableIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class Blocks {

	private final List<BasicBlock> blocks;
	private final Map<Label, Integer> heads;

	protected Blocks(List<BasicBlock> blocks, Map<Label, Integer> heads) {
		this.blocks = Check.notNull(blocks);
		this.heads = Check.notNull(heads);
	}

	// index for hashmap-based lookup
	private static Map<Label, Integer> buildIndex(List<BasicBlock> blocks) {
		Map<Label, Integer> heads = new HashMap<>();
		for (int i = 0; i < blocks.size(); i++) {
			BasicBlock b = blocks.get(i);
			Integer old = heads.put(b.label(), i);
			if (old != null) {
				throw new IllegalStateException("Redefining label " + b.label()
						+ " (in block #" + i + ", already defined in #" + old + ")");
			}
		}
		return heads;
	}

	public static Blocks of(List<BasicBlock> blocks) {
		Map<Label, Integer> index = buildIndex(blocks);
		return new Blocks(new ArrayList<>(blocks), index);
	}

	public Label entryLabel() {
		return blocks.get(0).label();
	}

	public BasicBlock getBlock(Label l) {
		Integer idx = heads.get(l);
		if (idx != null) {
			BasicBlock result = blocks.get(idx);
			assert (result.label().equals(l));
			return result;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	public Iterator<BasicBlock> blockIterator() {
		return new UnmodifiableIterator<>(blocks.iterator());
	}

}
