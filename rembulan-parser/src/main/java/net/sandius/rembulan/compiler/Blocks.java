package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.UnmodifiableIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class Blocks {

	private final ArrayList<BasicBlock> blocks;

	private final Map<Label, Integer> heads;

	protected Blocks(ArrayList<BasicBlock> blocks) {
		this.blocks = Check.notNull(blocks);

		// index for hashmap-based lookup
		this.heads = new HashMap<>();
		for (int i = 0; i < this.blocks.size(); i++) {
			BasicBlock b = this.blocks.get(i);
			heads.put(b.label(), i);
		}
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
