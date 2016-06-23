package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.BlockTermNode;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.JmpNode;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.compiler.ir.ToNext;
import net.sandius.rembulan.parser.util.Util;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockBuilder {

	private final List<Label> labelDefOrder;
	private final Map<Label, Integer> uses;
	private final Map<Label, ArrayList<IRNode>> blocks;
	private final Set<Label> pending;

	private int labelIdx;

	private Label currentLabel;

	public BlockBuilder() {
		this.labelDefOrder = new ArrayList<>();
		this.uses = new HashMap<>();
		this.blocks = new HashMap<>();
		this.pending = new HashSet<>();

		labelIdx = 0;

		add(newLabel());
	}

	public Label newLabel() {
		return new Label(labelIdx++);
	}

	private void appendToCurrentBlock(IRNode node) {
		if (currentLabel == null) {
			throw new IllegalStateException("Adding a node outside a block");
		}
		blocks.get(currentLabel).add(node);
	}

	public void add(Label label) {
		Check.notNull(label);
		pending.remove(label);
		if (blocks.put(label, new ArrayList<IRNode>()) != null) {
			throw new IllegalStateException("Label already used: " + label);
		}
		labelDefOrder.add(label);
		currentLabel = label;
	}

	public void add(BodyNode node) {
		Check.notNull(node);

		if (node instanceof JmpNode) {
			useLabel(((JmpNode) node).jmpDest());
		}

		if (currentLabel == null) {
			add(newLabel());
		}

		appendToCurrentBlock(node);
	}

	public void add(BlockTermNode node) {
		Check.notNull(node);

		if (node instanceof JmpNode) {
			useLabel(((JmpNode) node).jmpDest());
		}

		appendToCurrentBlock(node);
		currentLabel = null;
	}

	private int uses(Label l) {
		Check.notNull(l);
		Integer n = uses.get(l);
		return n != null ? n : 0;
	}

	private void useLabel(Label l) {
		Check.notNull(l);
		uses.put(l, uses(l) + 1);
		if (!blocks.containsKey(l)) {
			pending.add(l);
		}
	}

	private static <T> T last(List<T> ns) {
		int sz = ns.size();
		return sz > 0 ? ns.get(sz - 1) : null;
	}

	private static List<IRNode> copyList(List<IRNode> l) {
		return Collections.unmodifiableList(new ArrayList<>(l));
	}

	public Blocks build() {
		if (!pending.isEmpty()) {
			throw new IllegalStateException("Label(s) not defined: " + Util.iterableToString(pending, ", "));
		}

		ArrayList<BasicBlock> basicBlocks = new ArrayList<>();

		Label label = null;
		final ArrayList<IRNode> buf = new ArrayList<>();

		for (Label l : labelDefOrder) {
			if (label == null) {
				// this is the initial block
				label = l;
			}
			else if (uses(l) > 0 || last(buf) instanceof BlockTermNode) {
				// start of a new basic block

				IRNode last = last(buf);
				final List<IRNode> body;
				final BlockTermNode end;

				if (last instanceof BlockTermNode) {
					body = copyList(buf.subList(0, buf.size() - 1));
					end = (BlockTermNode) last;
				}
				else {
					body = copyList(buf);
					end = new ToNext(l);  // falling through
				}

				basicBlocks.add(new BasicBlock(label, body, end));

				label = l;
				buf.clear();
			}
			else {
				// label can be omitted: previous node falls through & is not a target of jumps
			}

			buf.addAll(blocks.get(l));
		}

		assert (label != null);

		// last pending block

		IRNode last = last(buf);
		if ((last instanceof BlockTermNode)) {
			basicBlocks.add(new BasicBlock(
					label,
					copyList(buf.subList(0, buf.size() - 1)),
					(BlockTermNode) last));
		}
		else {
			// falling off the end
			throw new IllegalStateException("Control reaches end of function");
		}

		return new Blocks(basicBlocks);
	}

}
