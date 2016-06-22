package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Iterator;

public class BlockBuilder {

	private final ArrayList<IRNode> insns;

	public BlockBuilder() {
		this.insns = new ArrayList<>();
	}

	public Iterator<IRNode> nodes() {
		return insns.iterator();
	}

	public void add(IRNode node) {
		Check.notNull(node);
		insns.add(node);
	}

}
