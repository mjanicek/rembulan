package net.sandius.rembulan.compiler.gen.mk2;

import net.sandius.rembulan.compiler.FunctionId;
import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.analysis.DependencyInfo;
import net.sandius.rembulan.compiler.analysis.SlotAllocInfo;
import net.sandius.rembulan.compiler.analysis.TypeInfo;
import net.sandius.rembulan.compiler.gen.BytecodeEmitter;
import net.sandius.rembulan.compiler.gen.ClassNameTranslator;
import net.sandius.rembulan.compiler.gen.CompiledClass;
import net.sandius.rembulan.compiler.gen.asm.ASMUtils;
import net.sandius.rembulan.compiler.gen.asm.InvokableMethods;
import net.sandius.rembulan.compiler.gen.asm.InvokeKind;
import net.sandius.rembulan.compiler.ir.UpVar;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.util.ByteVector;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class ASMBytecodeEmitter extends BytecodeEmitter {

	public final IRFunc fn;
	public final SlotAllocInfo slots;
	public final TypeInfo types;
	public final DependencyInfo deps;

	public final ClassNameTranslator classNameTranslator;

	private final String sourceFile;

	private final ClassNode classNode;

	private final ArrayList<String> upvalueFieldNames;

	private boolean verifyAndPrint;


	public ASMBytecodeEmitter(
			IRFunc fn,
			SlotAllocInfo slots,
			TypeInfo types,
			DependencyInfo deps,
			ClassNameTranslator classNameTranslator,
			String sourceFile) {

		this.fn = Check.notNull(fn);
		this.slots = Check.notNull(slots);
		this.types = Check.notNull(types);
		this.deps = Check.notNull(deps);

		this.classNameTranslator = Check.notNull(classNameTranslator);
		this.sourceFile = Check.notNull(sourceFile);

		classNode = new ClassNode();

		upvalueFieldNames = new ArrayList<>();

		String s = System.getProperty("net.sandius.rembulan.compiler.VerifyAndPrint");
		verifyAndPrint = s != null && "true".equals(s.trim().toLowerCase());
	}

	int kind() {
		return InvokeKind.adjust_nativeKind(InvokeKind.encode(fn.params().size(), fn.isVararg()));
	}

	String thisClassName() {
		return fn.id().toClassName(classNameTranslator);
	}

	Type thisClassType() {
		return ASMUtils.typeForClassName(thisClassName());
	}

	Type superClassType() {
		return Type.getType(InvokeKind.nativeClassForKind(kind()));
	}

	Type parentClassType() {
		FunctionId parentId = fn.id().parent();
		return parentId != null
				? ASMUtils.typeForClassName(parentId.toClassName(classNameTranslator))
				: null;
	}

	public Type savedStateClassType() {
		return Type.getType(Object.class);
	}

	Type invokeMethodType() {
		return InvokableMethods.invoke_method(kind()).getMethodType();
	}

	public boolean hasUpvalues() {
		return !fn.upvals().isEmpty();
	}

	public int numOfParameters() {
		return fn.params().size();
	}

	public boolean isVararg() {
		return fn.isVararg();
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

		List<FunctionId> nestedIds = new ArrayList<>(deps.nestedRefs());
		Collections.sort(nestedIds, FunctionId.LEXICOGRAPHIC_COMPARATOR);

		for (FunctionId childId : nestedIds) {
			String childClassName = childId.toClassName(classNameTranslator);
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


	public String instanceFieldName() {
		return "INSTANCE";
	}

	private FieldNode instanceField() {
		return new FieldNode(
				ACC_PUBLIC + ACC_FINAL + ACC_STATIC,
				instanceFieldName(),
				thisClassType().getDescriptor(),
				null,
				null);
	}

	InsnList instantiateNestedInstanceFields() {
		InsnList il = new InsnList();

		// TODO

		/*
		ReadOnlyArray<Prototype> nps = context().prototype().getNestedPrototypes();
		for (int i = 0; i < nps.size(); i++) {
			if (nestedInstanceKind(i) == ClassEmitter.NestedInstanceKind.Closed) {
				ReadOnlyArray<Prototype.UpvalueDesc> uvds = nps.get(i).getUpValueDescriptions();

				il.add(new VarInsnNode(ALOAD, 0));
				il.add(new TypeInsnNode(NEW, nestedInstanceFieldType(i).getInternalName()));
				il.add(new InsnNode(DUP));
				for (Prototype.UpvalueDesc uvd : uvds) {
					Check.isFalse(uvd.inStack);

					il.add(new VarInsnNode(ALOAD, 0));  // this
					il.add(new FieldInsnNode(
							GETFIELD,
							thisClassType().getInternalName(),
							getUpvalueFieldName(uvd.index),
							Type.getDescriptor(Upvalue.class)));
				}
				il.add(new MethodInsnNode(
						INVOKESPECIAL,
						nestedInstanceFieldType(i).getInternalName(),
						"<init>",
						Type.getMethodDescriptor(
								Type.VOID_TYPE,
								ASMUtils.fillTypes(Type.getType(Upvalue.class), uvds.size())),
						false));
				il.add(setNestedInstance(i));
			}
		}
		*/

		return il;
	}

	private static String toFieldName(String n) {
		return n;  // TODO
	}

	private static String ensureUnique(List<String> ss, String s) {
		int idx = 0;
		String prefix = s;

		while (ss.contains(s)) {
			s = prefix + "_" + (idx++);
		}

		return s;
	}

	private static String preferredUpvalueName(UpVar uv) {
		return "uv";  // TODO
	}


	private void setUpvalueFieldNames() {
		upvalueFieldNames.clear();
		for (int i = 0; i < fn.upvals().size(); i++) {
			UpVar uv = fn.upvals().get(i);
			upvalueFieldNames.add(toFieldName(ensureUnique(upvalueFieldNames, preferredUpvalueName(uv))));
		}
	}

	private void addUpvalueFields() {
		setUpvalueFieldNames();

		for (String name : upvalueFieldNames) {
			FieldNode fieldNode = new FieldNode(
					ACC_PROTECTED + ACC_FINAL,
					name,
					Type.getDescriptor(Upvalue.class),
					null,
					null);

			classNode.fields.add(fieldNode);
		}
	}

	public String getUpvalueFieldName(int idx) {
		String name = upvalueFieldNames.get(idx);
		if (name == null) {
			throw new IllegalArgumentException("upvalue field name is null for index " + idx);
		}
		return name;
	}

	public ClassNode classNode() {
		classNode.version = V1_7;
		classNode.access = ACC_PUBLIC + ACC_SUPER;
		classNode.name = thisClassType().getInternalName();
		classNode.superName = superClassType().getInternalName();
		classNode.sourceFile = sourceFile;

		addInnerClassLinks();

		if (!hasUpvalues()) {
			classNode.fields.add(instanceField());
		}
//		addNestedInstanceFields();

		addUpvalueFields();

		ConstructorMethod ctor = new ConstructorMethod(this);
		RunMethod runMethod = new RunMethod(this);

		classNode.methods.add(ctor.methodNode());
		classNode.methods.add(new InvokeMethod(this, runMethod).methodNode());
		classNode.methods.add(new ResumeMethod(this, runMethod).methodNode());
		classNode.methods.add(runMethod.methodNode());

		if (runMethod.usesSnapshotMethod()) {
			classNode.methods.add(runMethod.snapshotMethodNode());
		}

		StaticConstructorMethod staticCtor = new StaticConstructorMethod(this, ctor);
		if (!staticCtor.isEmpty()) {
			classNode.methods.add(staticCtor.methodNode());
		}

		return classNode;
	}

	private byte[] classNodeToBytes(ClassNode classNode) {
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		byte[] bytes = writer.toByteArray();

		// verify bytecode

		if (verifyAndPrint) {
			ClassReader reader = new ClassReader(bytes);
			ClassVisitor tracer = new TraceClassVisitor(new PrintWriter(System.out));
			ClassVisitor checker = new CheckClassAdapter(tracer, true);
			reader.accept(checker, 0);
		}

		return bytes;
	}

	@Override
	public CompiledClass emit() {
		ClassNode classNode = classNode();
		byte[] bytes = classNodeToBytes(classNode);
		return new CompiledClass(thisClassName(), ByteVector.wrap(bytes));
	}

}
