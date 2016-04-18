package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.compiler.gen.PrototypeContext;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

public class ClassEmitter {

	private final PrototypeContext context;
	private final ClassNode classNode;
	private final int numOfParameters;
	private final boolean isVararg;

	private final ArrayList<String> upvalueFieldNames;

	private final InvokeMethodEmitter invokeMethodEmitter;
	private final ResumeMethodEmitter resumeMethodEmitter;

	private final RunMethodEmitter runMethodEmitter;
	private final SnapshotMethodEmitter snapshotMethodEmitter;

	public ClassEmitter(PrototypeContext context, int numOfParameters, boolean isVararg) {
		this.context = Check.notNull(context);
		this.classNode = new ClassNode();
		this.upvalueFieldNames = new ArrayList<>();
		this.numOfParameters = numOfParameters;
		this.isVararg = isVararg;

		this.invokeMethodEmitter = new InvokeMethodEmitter(this);
		this.resumeMethodEmitter = new ResumeMethodEmitter(this);
		this.runMethodEmitter = new RunMethodEmitter(this);
		this.snapshotMethodEmitter = new SnapshotMethodEmitter(this);
	}

	protected Type thisClassType() {
		return ASMUtils.typeForClassName(context.className());
	}

	protected Type parentClassType() {
		String cn = context.parentClassName();
		return cn != null ? ASMUtils.typeForClassName(cn) : null;
	}

	protected int kind() {
		return InvokeKind.adjust_nativeKind(InvokeKind.encode(numOfParameters, isVararg));
	}

	protected PrototypeContext context() {
		return context;
	}

	protected int numOfParameters() {
		return numOfParameters;
	}

	protected boolean isVararg() {
		return isVararg;
	}

	protected Type invokeMethodType() {
		return InvokableMethods.invoke_method(kind()).getMethodType();
	}

	protected Type superClassType() {
		return Type.getType(InvokeKind.nativeClassForKind(kind()));
	}

	public void begin() {
		classNode.version = V1_7;
		classNode.access = ACC_PUBLIC + ACC_SUPER;
		classNode.name = thisClassType().getInternalName();
		classNode.superName = superClassType().getInternalName();
		classNode.sourceFile = context.prototype().getShortSource();

		addInnerClassLinks();

		addUpvalueFields();

		classNode.methods.add(constructorNode());
	}

	public void end() {
		invokeMethod().end();
		resumeMethod().end();

		classNode.methods.add(invokeMethod().node());
		classNode.methods.add(resumeMethod().node());
		classNode.methods.add(runMethod().node());

		if (runMethod().isResumable()) {
			snapshotMethod().end();
			classNode.methods.add(snapshotMethod().node());
		}
	}

	public void accept(ClassVisitor visitor) {
		classNode.accept(visitor);
	}

	public Type upvalueType() {
		return Type.getType(Upvalue.class);
	}

	private String toJavaName(String n) {
		return n;
	}

	private void addInnerClassLinks() {
		String ownInternalName = thisClassType().getInternalName();

		// parent
		if (parentClassType() != null) {
			String parentInternalName = parentClassType().getInternalName();

			// assume (parentInternalName + "$") is the prefix of ownInternalName
			String suffix = ownInternalName.substring(parentInternalName.length() + 1);

			classNode.innerClasses.add(new InnerClassNode(
					ownInternalName,
					parentInternalName,
					suffix,
					ACC_PUBLIC + ACC_STATIC));
		}

		// children
		for (Prototype child : context.prototype().getNestedPrototypes()) {
			String childClassName = context.compilationContext().prototypeClassName(child);
			if (childClassName != null) {
				String childInternalName = ASMUtils.typeForClassName(childClassName).getInternalName();

				// assume (ownInternalName + "$") is the prefix of childName
				String suffix = childInternalName.substring(ownInternalName.length() + 1);

				classNode.innerClasses.add(new InnerClassNode(
						childInternalName,
						ownInternalName,
						suffix,
						ACC_PUBLIC + ACC_STATIC));
			}
		}

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

	protected ClassNode classNode() {
		return classNode;
	}

	public RunMethodEmitter runMethod() {
		return runMethodEmitter;
	}

	public InvokeMethodEmitter invokeMethod() {
		return invokeMethodEmitter;
	}

	public ResumeMethodEmitter resumeMethod() {
		return resumeMethodEmitter;
	}

	public SnapshotMethodEmitter snapshotMethod() {
		return snapshotMethodEmitter;
	}

}
