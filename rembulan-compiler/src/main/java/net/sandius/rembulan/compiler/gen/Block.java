package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntBuffer;

import java.util.ArrayList;

public class Block {

	public final ArrayList<BlockNode> nodes;
	public final IntBuffer prev;
	public final IntBuffer next;

	public Slots slots;

	private Block(Slots slots, ArrayList<BlockNode> nodes, IntBuffer prev, IntBuffer next) {
		Check.notNull(slots);
		Check.notNull(nodes);
		Check.notNull(prev);
		Check.notNull(next);

		this.slots = slots;
		this.nodes = nodes;
		this.prev = prev;
		this.next = next;
	}

	public static Block newBlock(int pc, int insn, int slotSize, IntBuffer prev, IntBuffer next) {
		ArrayList<BlockNode> l = new ArrayList<>();
		l.add(Instruction.valueOf(pc, insn));
		return new Block(Slots.init(slotSize), l, prev, next);
	}

	public void merge(Block that) {
		nodes.addAll(that.nodes);
		next.clear();
		next.append(that.next);
	}

	public void shift(int idx) {
		for (int j = 0; j < prev.length(); j++) {
			int tgt = prev.get(j);
			assert (tgt != idx);
			prev.set(j, tgt > idx ? tgt - 1 : tgt);
		}

		for (int j = 0; j < next.length(); j++) {
			int tgt = next.get(j);
			assert (tgt != idx);
			next.set(j, tgt > idx ? tgt - 1 : tgt);
		}
	}

	public void renumber(int from, int to) {
		prev.replaceValue(from, to);
		next.replaceValue(from, to);
	}

	public void erase(int idx) {
		prev.removeValue(idx);
		next.removeValue(idx);
	}

	public int getCost() {
		int cost = 0;
		for (BlockNode node : nodes) {
			cost += node.getCost();
		}
		return cost;
	}

	public Slots slots() {
		return slots;
	}

}
