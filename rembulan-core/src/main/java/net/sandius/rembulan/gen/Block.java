package net.sandius.rembulan.gen;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntBuffer;

import java.util.ArrayList;

public class Block {

	public final ArrayList<BlockNode> instructions;
	public final IntBuffer prev;
	public final IntBuffer next;

	private Block(ArrayList<BlockNode> instructions, IntBuffer prev, IntBuffer next) {
		Check.notNull(instructions);
		Check.notNull(prev);
		Check.notNull(next);

		this.instructions = instructions;
		this.prev = prev;
		this.next = next;
	}

	public static Block newBlock(int insn, IntBuffer prev, IntBuffer next) {
		ArrayList<BlockNode> l = new ArrayList<>();
		l.add(Instruction.valueOf(insn));
		return new Block(l, prev, next);
	}

	public void merge(Block that) {
		instructions.addAll(that.instructions);
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
		for (BlockNode node : instructions) {
			cost += node.getCost();
		}
		return cost;
	}

}
