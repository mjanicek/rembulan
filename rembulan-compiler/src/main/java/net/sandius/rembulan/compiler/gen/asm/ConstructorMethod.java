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

package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.ir.UpVar;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Arrays;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;

class ConstructorMethod {

	private final ASMBytecodeEmitter context;
	private final RunMethod runMethod;

	public ConstructorMethod(ASMBytecodeEmitter context, RunMethod runMethod) {
		this.context = Objects.requireNonNull(context);
		this.runMethod = Objects.requireNonNull(runMethod);
	}

	public Type methodType() {
		Type[] args = new Type[context.fn.upvals().size()];
		Arrays.fill(args, Type.getType(Variable.class));
		return Type.getMethodType(Type.VOID_TYPE, args);
	}

	public MethodNode methodNode() {

		MethodNode node = new MethodNode(
				ACC_PUBLIC,
				"<init>",
				methodType().getDescriptor(),
				null,
				null);


		InsnList il = node.instructions;

		LabelNode begin = new LabelNode();
		LabelNode end = new LabelNode();

		node.localVariables.add(new LocalVariableNode("this", context.thisClassType().getDescriptor(), null, begin, end, 0));

		il.add(begin);

		// superclass constructor
		il.add(new VarInsnNode(ALOAD, 0));
		il.add(new MethodInsnNode(
				INVOKESPECIAL,
				context.superClassType().getInternalName(),
				"<init>",
				Type.getMethodType(Type.VOID_TYPE).getDescriptor(),
				false));

		// initialise upvalue fields
		int idx = 0;
		for (UpVar uv : context.fn.upvals()) {
			String name = context.getUpvalueFieldName(uv);

			il.add(new VarInsnNode(ALOAD, 0));  // this
			il.add(new VarInsnNode(ALOAD, 1 + idx));  // upvalue #i
			il.add(new FieldInsnNode(PUTFIELD,
					context.thisClassType().getInternalName(),
					name,
					Type.getDescriptor(Variable.class)));

			node.localVariables.add(new LocalVariableNode(name, Type.getDescriptor(Variable.class), null, begin, end, idx));

			idx++;
		}

		// instantiate fields for closures that have no open upvalues
		for (RunMethod.ClosureFieldInstance cfi : runMethod.closureFields()) {
			context.fields().add(cfi.fieldNode());
			il.add(cfi.instantiateInsns());
		}

		il.add(new InsnNode(RETURN));

		il.add(end);

		node.maxStack = 2;
		node.maxLocals = context.fn.upvals().size() + 1;

		return node;
	}

}
