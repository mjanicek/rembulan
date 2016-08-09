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

package net.sandius.rembulan.compiler.gen.asm.helpers;

import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class LuaStateMethods {

	private LuaStateMethods() {
		// not to be instantiated
	}

	private static Type selfTpe() {
		return Type.getType(LuaState.class);
	}

	public static InsnList newTable(int array, int hash) {
		InsnList il = new InsnList();

		il.add(ASMUtils.loadInt(array));
		il.add(ASMUtils.loadInt(hash));

		il.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"newTable",
				Type.getMethodType(
						Type.getType(Table.class),
						Type.INT_TYPE,
						Type.INT_TYPE).getDescriptor(),
				false));

		return il;
	}

}
