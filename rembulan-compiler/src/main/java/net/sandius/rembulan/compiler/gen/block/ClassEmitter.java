package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.PrototypeContext;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_7;

public class ClassEmitter {

	private final PrototypeContext context;
	private final ClassVisitor visitor;

	private final ClassNode classNode;

	private final ArrayList<String> upvalueFieldNames;


	public ClassEmitter(PrototypeContext context, ClassVisitor visitor) {
		this.context = Check.notNull(context);
		this.visitor = Check.notNull(visitor);

		this.classNode = new ClassNode();

		this.upvalueFieldNames = new ArrayList<>();
	}

	protected Type thisClassType() {
		return Type.getType(context.className());
	}

	protected Type superClassType() {
		return Type.getType(Function.class);
	}

	public void begin() {
		classNode.version = V1_7;
		classNode.access = ACC_PUBLIC + ACC_SUPER;
		classNode.name = thisClassType().getInternalName();
		classNode.superName = superClassType().getInternalName();
		classNode.sourceFile = context.prototype().getShortSource();

		addUpvalueFields();

		classNode.methods.add(constructorNode());
	}

	public void end() {
		visitor.visitEnd();
		classNode.accept(visitor);
	}

	public byte[] toBytes() {
		return null;  // TODO
	}

	public Type upvalueType() {
		return Type.getType(Upvalue.class);
	}

	private String toJavaName(String n) {
		return n;
	}

	private String findUniqueUpvalueName(String prefix) {
		if (upvalueFieldNames.contains(prefix)) {
			String name;
			int idx = 1;
			do {
				name = prefix + "_" + idx++;
			} while (upvalueFieldNames.contains(name));
			return name;
		}
		else {
			return prefix;
		}
	}

	public String getUpvalueFieldName(int idx) {
		String name = upvalueFieldNames.get(idx);
		if (name == null) {
			throw new IllegalArgumentException("upvalue field name is null for index " + idx);
		}
		return name;
	}

	private void setUpvalueFieldNames(ReadOnlyArray<Prototype.UpvalueDesc> upvalueDescs) {
		upvalueFieldNames.clear();
		for (int i = 0; i < upvalueDescs.size(); i++) {
			Prototype.UpvalueDesc uvd = upvalueDescs.get(i);
			upvalueFieldNames.add(uvd.name == null ? "uv_" + i : null);
		}
		for (int i = 0; i < upvalueDescs.size(); i++) {
			Prototype.UpvalueDesc uvd = upvalueDescs.get(i);
			if (uvd.name != null) {
				upvalueFieldNames.set(i, findUniqueUpvalueName(toJavaName(uvd.name)));
			}
		}
	}

	private void addUpvalueFields() {
		setUpvalueFieldNames(context.prototype().getUpValueDescriptions());

		for (String name : upvalueFieldNames) {
			FieldNode fieldNode = new FieldNode(
					ACC_PROTECTED + ACC_FINAL,
					name,
					upvalueType().getDescriptor(),
					null,
					null);

			classNode.fields.add(fieldNode);
		}
	}

	private MethodNode constructorNode() {
		ReadOnlyArray<Prototype.UpvalueDesc> uvd = context.prototype().getUpValueDescriptions();

		Type[] args = new Type[uvd.size()];
		Arrays.fill(args, upvalueType());
		Type ctorMethodType = Type.getMethodType(Type.VOID_TYPE, args);

		MethodNode ctorMethodNode = new MethodNode(
				ACC_PUBLIC,
				"<init>",
				ctorMethodType.getDescriptor(),
				null,
				null);

		InsnList il = ctorMethodNode.instructions;

		LabelNode begin = new LabelNode();
		LabelNode end = new LabelNode();

		ctorMethodNode.localVariables.add(new LocalVariableNode("this", thisClassType().getDescriptor(), null, begin, end, 0));

		il.add(begin);

		// superclass constructor
		il.add(new VarInsnNode(ALOAD, 0));
		il.add(new MethodInsnNode(
				INVOKESPECIAL,
				superClassType().getInternalName(),
				"<init>",
				Type.getMethodType(Type.VOID_TYPE).getDescriptor(),
				false));

		for (int i = 0; i < uvd.size(); i++) {
			String name = getUpvalueFieldName(i);

			il.add(new VarInsnNode(ALOAD, 0));  // this
			il.add(new VarInsnNode(ALOAD, 1 + i));  // upvalue #i
			il.add(new FieldInsnNode(PUTFIELD, thisClassType().getInternalName(), name, upvalueType().getDescriptor()));

			ctorMethodNode.localVariables.add(new LocalVariableNode(name, upvalueType().getDescriptor(), null, begin, end, i));
		}

		il.add(new InsnNode(RETURN));

		il.add(end);

		ctorMethodNode.maxStack = 2;
		ctorMethodNode.maxLocals = args.length + 1;

		return ctorMethodNode;
	}

	public CodeEmitter code() {
		CodeEmitter emitter = new CodeEmitter(this, context);
		classNode.methods.add(emitter.node());
		return emitter;
	}

}
