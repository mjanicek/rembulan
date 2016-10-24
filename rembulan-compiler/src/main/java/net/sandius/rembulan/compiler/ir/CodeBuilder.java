/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.parser.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CodeBuilder {

	private final Map<Label, Integer> uses;
	private final Set<Label> pending;
	private final Set<Label> visited;

	private final List<BasicBlock> basicBlocks;

	private int labelIdx;

	private Block currentBlock;
	private int currentLine;

	public CodeBuilder() {
		this.uses = new HashMap<>();
		this.pending = new HashSet<>();
		this.visited = new HashSet<>();

		this.basicBlocks = new ArrayList<>();

		labelIdx = 0;

		this.currentBlock = null;
		this.currentLine = 0;

		add(newLabel());
	}

	public Label newLabel() {
		return new Label(labelIdx++);
	}

	public boolean isInBlock() {
		return currentBlock != null;
	}

	private void closeCurrentBlock(BlockTermNode end) {
		if (currentBlock != null) {
			basicBlocks.add(currentBlock.toBasicBlock(end));
		}
		else {
			// No block is currently open: attempting to close unreachable code, may be ignored.
		}
		currentBlock = null;
	}

	private void appendToCurrentBlock(BodyNode node) {
		if (currentBlock != null) {
			currentBlock.add(node);
		}
		else {
			// Adding a node outside a block: this is unreachable code, don't add it.
			// This happens when an unconditional jump is not immediately followed
			// by a label.
		}
	}

	public void add(Label label) {
		Objects.requireNonNull(label);
		pending.remove(label);

		if (!visited.add(label)) {
			throw new IllegalStateException("Label already used: " + label);
		}

		if (currentBlock != null) {
			closeCurrentBlock(new ToNext(label));
		}

		assert (currentBlock == null);

		currentBlock = new Block(label);
		if (currentLine > 0) {
			addLine(currentLine);
		}
	}

	public void add(BodyNode node) {
		Objects.requireNonNull(node);

		if (node instanceof JmpNode) {
			useLabel(((JmpNode) node).jmpDest());
		}

		appendToCurrentBlock(node);
	}

	public void add(BlockTermNode node) {
		Objects.requireNonNull(node);

		if (node instanceof JmpNode) {
			useLabel(((JmpNode) node).jmpDest());
		}

		closeCurrentBlock(node);
//		currentLine = 0;
	}

	public void addBranch(Branch.Condition cond, Label dest) {
		Label next = newLabel();
		add(new Branch(cond, dest, next));
		add(next);
	}

	private void addLine(int line) {
		add(new Line(line));
	}

	public void atLine(int line) {
		if (line > 0 && line != currentLine) {
			currentLine = line;
			addLine(line);
		}
	}

	private int uses(Label l) {
		Objects.requireNonNull(l);
		Integer n = uses.get(l);
		return n != null ? n : 0;
	}

	private void useLabel(Label l) {
		Objects.requireNonNull(l);
		uses.put(l, uses(l) + 1);
		if (!visited.contains(l)) {
			pending.add(l);
		}
	}

	private static class Block {

		private final Label label;
		private final List<BodyNode> body;

		private Block(Label label) {
			this.label = label;
			this.body = new ArrayList<>();
		}

		public void add(BodyNode n) {
			body.add(n);
		}

		public BasicBlock toBasicBlock(BlockTermNode end) {
			return new BasicBlock(label, Collections.unmodifiableList(body), end);
		}

	}

	public Code build() {
		if (!pending.isEmpty()) {
			throw new IllegalStateException("Label(s) not defined: " + Util.iterableToString(pending, ", "));
		}
		if (currentBlock != null) {
			throw new IllegalStateException("Control reaches end of function");
		}

		return Code.of(basicBlocks);
	}

}
