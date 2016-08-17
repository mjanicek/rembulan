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

import net.sandius.rembulan.compiler.gen.asm.helpers.ASMUtils;
import net.sandius.rembulan.compiler.gen.asm.helpers.UtilMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.VariableMethods;
import net.sandius.rembulan.compiler.ir.Var;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Variable;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

class InvokeMethod {

	private final ASMBytecodeEmitter context;
	private final RunMethod runMethod;

	public InvokeMethod(ASMBytecodeEmitter context, RunMethod runMethod) {
		this.context = Check.notNull(context);
		this.runMethod = Check.notNull(runMethod);
	}

	public MethodNode methodNode() {
		MethodNode node = new MethodNode(
				ACC_PUBLIC,
				"invoke",
				context.invokeMethodType().getDescriptor(),
				null,
				runMethod.throwsExceptions());

		InsnList il = node.instructions;
		List<LocalVariableNode> locals = node.localVariables;

		LabelNode begin = new LabelNode();
		LabelNode end = new LabelNode();

		int invokeKind = context.kind();

		il.add(begin);

		// a (slotIdx -> paramIdx) map
		int[] slotParamMap = new int[context.slots.numSlots()];
		Arrays.fill(slotParamMap, -1);

		for (int paramIdx = 0; paramIdx < context.fn.params().size(); paramIdx++) {
			int slotIdx = context.slots.slotOf(context.fn.params().get(paramIdx));
			assert (slotParamMap[slotIdx] == -1);
			slotParamMap[slotIdx] = paramIdx;
		}

		if (invokeKind > 0) {
			il.add(new VarInsnNode(ALOAD, 0));  // this
			il.add(new VarInsnNode(ALOAD, 1));  // context
			il.add(ASMUtils.loadInt(0));  // resumption point

			// we have (invokeKind - 1) standalone parameters, mapping them onto numSlots

			for (int paramIdx : slotParamMap) {
				if (paramIdx < 0) {
					// slot unused
					il.add(new InsnNode(ACONST_NULL));
				}
				else {
					// used by the parameter #paramIdx
					Var param = context.fn.params().get(paramIdx);
					boolean reified = context.types.isReified(param);

					if (reified) {
						il.add(new TypeInsnNode(NEW, Type.getInternalName(Variable.class)));
						il.add(new InsnNode(DUP));
					}

					il.add(new VarInsnNode(ALOAD, 2 + paramIdx));

					if (reified) {
						il.add(VariableMethods.constructor());
					}
				}
			}
		}
		else {
			// variable number of parameters, encoded in an array at position 2

			int lv_varargsSize = 3;
			int lv_varargs = 4;

			if (context.isVararg()) {

				LabelNode l_v_begin = new LabelNode();
				LabelNode l_v_nonempty = new LabelNode();
				LabelNode l_v_empty = new LabelNode();
				LabelNode l_v_done = new LabelNode();

				il.add(new VarInsnNode(ALOAD, 2));
				il.add(new InsnNode(ARRAYLENGTH));

				int numParams = context.numOfParameters();

				if (numParams > 0) {
					il.add(ASMUtils.loadInt(context.numOfParameters()));
					il.add(new InsnNode(ISUB));
				}
				il.add(new VarInsnNode(ISTORE, lv_varargsSize));

				il.add(l_v_begin);

				il.add(new VarInsnNode(ILOAD, lv_varargsSize));
				il.add(new JumpInsnNode(IFLE, l_v_empty));

				// nonempty varargs

				// varargs = new Object[varargsSize];
				il.add(new VarInsnNode(ILOAD, lv_varargsSize));
				il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
				il.add(new VarInsnNode(ASTORE, lv_varargs));

				il.add(l_v_nonempty);

				// call System.arraycopy(src, srcPos, dest, destPos, len)
				il.add(new VarInsnNode(ALOAD, 2));  // src
				il.add(ASMUtils.loadInt(numParams));  // srcPos
				il.add(new VarInsnNode(ALOAD, lv_varargs));  // dest
				il.add(ASMUtils.loadInt(0));  // destPos
				il.add(new VarInsnNode(ILOAD, lv_varargsSize));  // len
				il.add(new MethodInsnNode(
						INVOKESTATIC,
						Type.getInternalName(System.class),
						"arraycopy",
						Type.getMethodDescriptor(
								Type.VOID_TYPE,
								Type.getType(Object.class),
								Type.INT_TYPE,
								Type.getType(Object.class),
								Type.INT_TYPE,
								Type.INT_TYPE),
						false));

				il.add(new JumpInsnNode(GOTO, l_v_done));

				// empty varargs
				il.add(l_v_empty);
				il.add(new FrameNode(F_APPEND, 1, new Object[] { Opcodes.INTEGER }, 0, null));

				// varargs = new Object[0];
				il.add(ASMUtils.loadInt(0));
				il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
				il.add(new VarInsnNode(ASTORE, lv_varargs));

				il.add(l_v_done);
				il.add(new FrameNode(F_APPEND, 1, new Object[] {
							ASMUtils.arrayTypeFor(Object.class).getInternalName()
						}, 0, null));

				locals.add(new LocalVariableNode("sz", Type.INT_TYPE.getDescriptor(), null, l_v_begin, end, lv_varargsSize));
				locals.add(new LocalVariableNode("varargs", ASMUtils.arrayTypeFor(Object.class).getDescriptor(), null, l_v_nonempty, l_v_empty, lv_varargs));
				locals.add(new LocalVariableNode("varargs", ASMUtils.arrayTypeFor(Object.class).getDescriptor(), null, l_v_done, end, lv_varargs));
			}

			il.add(new VarInsnNode(ALOAD, 0));  // this
			il.add(new VarInsnNode(ALOAD, 1));  // context
			il.add(ASMUtils.loadInt(0));  // resumption point

			if (context.isVararg()) {
				il.add(new VarInsnNode(ALOAD, lv_varargs));
			}

			// load #numOfParameters, mapping them onto #numOfRegisters

			for (int paramIdx : slotParamMap) {
				if (paramIdx < 0) {
					// slot unused
					il.add(new InsnNode(ACONST_NULL));
				}
				else {
					// used by the parameter #paramIdx
					Var param = context.fn.params().get(paramIdx);
					boolean reified = context.types.isReified(param);

					if (reified) {
						il.add(new TypeInsnNode(NEW, Type.getInternalName(Variable.class)));
						il.add(new InsnNode(DUP));
					}

					il.add(new VarInsnNode(ALOAD, 2));  // TODO: use dup instead?
					il.add(UtilMethods.getArrayElementOrNull(paramIdx));

					if (reified) {
						il.add(VariableMethods.constructor());
					}
				}
			}

		}

		il.add(runMethod.methodInvokeInsn());

		il.add(new InsnNode(RETURN));
		il.add(end);

		locals.add(new LocalVariableNode("this", context.thisClassType().getDescriptor(), null, begin, end, 0));
		locals.add(new LocalVariableNode("context", Type.getDescriptor(ExecutionContext.class), null, begin, end, 1));
		if (invokeKind > 0) {
			for (int i = 0; i < invokeKind; i++) {
				locals.add(new LocalVariableNode("arg_" + i, Type.getDescriptor(Object.class), null, begin, end, 2 + i));
			}
			// TODO: maxLocals, maxStack
		}
		else {
			locals.add(new LocalVariableNode("args", ASMUtils.arrayTypeFor(Object.class).getDescriptor(), null, begin, end, 2));
			// TODO: maxLocals, maxStack
		}

		return node;
	}

}
