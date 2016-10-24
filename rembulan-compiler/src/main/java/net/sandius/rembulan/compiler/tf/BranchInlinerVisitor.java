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

package net.sandius.rembulan.compiler.tf;

import net.sandius.rembulan.compiler.analysis.TypeInfo;
import net.sandius.rembulan.compiler.analysis.types.LuaTypes;
import net.sandius.rembulan.compiler.analysis.types.Type;
import net.sandius.rembulan.compiler.ir.Branch;
import net.sandius.rembulan.compiler.ir.Jmp;
import net.sandius.rembulan.compiler.ir.ToNext;

import java.util.Objects;

class BranchInlinerVisitor extends CodeTransformerVisitor {

	private final TypeInfo types;
	private Boolean inline;

	public BranchInlinerVisitor(TypeInfo types) {
		this.types = Objects.requireNonNull(types);
	}

	@Override
	public void visit(Branch branch) {
		try {
			inline = null;
			branch.condition().accept(this);
			if (inline != null) {
				if (inline) {
					setEnd(new Jmp(branch.jmpDest()));
				}
				else {
					setEnd(new ToNext(branch.next()));
				}
			}
		}
		finally {
			inline = null;
		}
	}

	@Override
	public void visit(Branch.Condition.Nil cond) {
		Type t = types.typeOf(cond.addr());
		if (t.isSubtypeOf(LuaTypes.NIL)) {
			inline = Boolean.TRUE;
		}
		else if (t.isSubtypeOf(LuaTypes.ANY) && !t.equals(LuaTypes.ANY)) {
			inline = Boolean.FALSE;
		}
		else {
			inline = null;
		}
	}

	@Override
	public void visit(Branch.Condition.Bool cond) {
		Type t = types.typeOf(cond.addr());
		if (t.isSubtypeOf(LuaTypes.NIL)) {
			// t evaluates to false
			inline = !cond.expected();
		}
		else if (t.isSubtypeOf(LuaTypes.ANY) && !t.equals(LuaTypes.ANY) && !t.isSubtypeOf(LuaTypes.BOOLEAN)) {
			// t evaluates to true
			inline = cond.expected();
		}
		else {
			inline = null;
		}
	}

	@Override
	public void visit(Branch.Condition.NumLoopEnd cond) {
		inline = null;
	}

}
