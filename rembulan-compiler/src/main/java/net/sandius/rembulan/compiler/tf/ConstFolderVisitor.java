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

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.compiler.analysis.TypeInfo;
import net.sandius.rembulan.compiler.analysis.types.LiteralType;
import net.sandius.rembulan.compiler.analysis.types.Type;
import net.sandius.rembulan.compiler.ir.BinOp;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.LoadConst;
import net.sandius.rembulan.compiler.ir.ToNumber;
import net.sandius.rembulan.compiler.ir.UnOp;
import net.sandius.rembulan.compiler.ir.Val;
import net.sandius.rembulan.util.Check;

public class ConstFolderVisitor extends CodeTransformerVisitor {

	private final TypeInfo typeInfo;

	public ConstFolderVisitor(TypeInfo typeInfo) {
		this.typeInfo = Check.notNull(typeInfo);
	}

	private static LoadConst objectToLoadConstNode(Val dest, Object o) {
		Check.notNull(dest);

		if (o instanceof Number) {
			Number n = (Number) o;
			if (n instanceof Double || n instanceof Float) {
				return new LoadConst.Flt(dest, n.doubleValue());
			}
			else {
				return new LoadConst.Int(dest, n.longValue());
			}
		}
		else if (o instanceof ByteString) {
			return new LoadConst.Str(dest, (ByteString) o);
		}
		else if (o instanceof Boolean) {
			return new LoadConst.Bool(dest, ((Boolean) o).booleanValue());
		}
		else {
			return null;
		}
	}

	private void replace(BodyNode oldNode, BodyNode newNode) {
		Check.notNull(oldNode);
		Check.notNull(newNode);

		int idx = currentBody().indexOf(oldNode);
		if (idx < 0) {
			throw new IllegalStateException("Body node not found in current block: " + oldNode);
		}
		else {
			currentBody().set(idx, newNode);
		}
	}

	private void replaceIfLiteral(BodyNode node, Val v) {
		Type resultType = typeInfo.typeOf(v);
		if (resultType instanceof LiteralType) {
			LiteralType<?> lt = (LiteralType<?>) resultType;
			LoadConst loadNode = objectToLoadConstNode(v, lt.value());
			if (loadNode != null) {
				replace(node, loadNode);
			}
		}
	}

	@Override
	public void visit(BinOp node) {
		replaceIfLiteral(node, node.dest());
	}

	@Override
	public void visit(UnOp node) {
		replaceIfLiteral(node, node.dest());
	}

	@Override
	public void visit(ToNumber node) {
		replaceIfLiteral(node, node.dest());
	}

}
