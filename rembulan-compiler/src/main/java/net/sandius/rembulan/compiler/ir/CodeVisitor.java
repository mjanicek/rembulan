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

import net.sandius.rembulan.compiler.IRFunc;

import java.util.Iterator;

public class CodeVisitor extends IRVisitor {

	public CodeVisitor(IRVisitor visitor) {
		super(visitor);
	}

	public CodeVisitor() {
		super();
	}

	public void visit(IRFunc func) {
		visit(func.code());
	}

	public void visit(Code code) {
		Iterator<BasicBlock> it = code.blockIterator();
		while (it.hasNext()) {
			BasicBlock b = it.next();
			visit(b);
		}
	}

	public void visit(BasicBlock block) {
		visit(block.label());
		for (IRNode n : block.body()) {
			n.accept(this);
		}
		block.end().accept(this);
	}

}
