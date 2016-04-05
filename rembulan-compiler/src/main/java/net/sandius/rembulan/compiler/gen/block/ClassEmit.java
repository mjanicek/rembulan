package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.PrototypeContext;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ClassEmit {

	private final PrototypeContext context;
	private final ClassVisitor visitor;

	public ClassEmit(PrototypeContext context, ClassVisitor visitor) {
		this.context = Check.notNull(context);
		this.visitor = Check.notNull(visitor);
	}

	protected Type thisType() {
		return Type.getType(context.className());
	}

	public void _begin() {
		visitor.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, thisType().getInternalName(), null, Type.getInternalName(Function.class), null);
		visitor.visitSource(context.prototype().getShortSource(), null);

		_fields();
		_constructor();
	}

	public void _end() {
		visitor.visitEnd();
	}

	public String _upvalue_field_name(int idx) {
		String n = context.upvalueName(idx);
		if (n != null) {
			return n;  // FIXME: make sure it's a valid name, & that it's unique!
		}
		else {
			return "uv_" + idx;
		}
	}

	public void _fields() {
		int i = 0;
		for (Prototype.UpvalueDesc uvd : context.prototype().getUpValueDescriptions()) {
			String name = _upvalue_field_name(i);
			visitor.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, name, Type.getType(Upvalue.class).getDescriptor(), null, null);
			i++;
		}
	}

	public void _constructor() {

		Type[] args = new Type[context.prototype().getUpValueDescriptions().size()];
		Type at = Type.getType(Upvalue.class);
		for (int i = 0; i < args.length; i++) {
			args[i] = at;
		}

		Type ctorType = Type.getMethodType(Type.VOID_TYPE, args);

		MethodVisitor mv = visitor.visitMethod(Opcodes.ACC_PUBLIC, "<init>", ctorType.getDescriptor(), null, null);

		Label begin = new Label();
		mv.visitLabel(begin);

		int i = 0;
		for (Prototype.UpvalueDesc uvd : context.prototype().getUpValueDescriptions()) {
			String name = _upvalue_field_name(i);
			mv.visitVarInsn(Opcodes.ALOAD, 0);  // this
			mv.visitVarInsn(Opcodes.ALOAD, 1 + i);  // the argument
			mv.visitFieldInsn(Opcodes.PUTFIELD, thisType().getInternalName(), name, Type.getType(Upvalue.class).getDescriptor());
			i++;
		}

		Label end = new Label();
		mv.visitLabel(end);

		mv.visitLocalVariable("this", thisType().getDescriptor(), null, begin, end, 0);  // this
		for (int j = 0; j < args.length; j++) {
			mv.visitLocalVariable(_upvalue_field_name(j), at.getDescriptor(), null, begin, end, 1 + j);
		}

		mv.visitMaxs(2, args.length + 1);
		mv.visitEnd();
	}

	public Emit _code_emit() {
		Type methodType = Type.getMethodType(
				Type.VOID_TYPE,
				Type.getType(LuaState.class),
				Type.getType(ObjectSink.class),
				Type.INT_TYPE
		);

		MethodVisitor mv = visitor.visitMethod(Opcodes.ACC_PRIVATE, "run", methodType.getDescriptor(),
				null,
				new String[] { Type.getInternalName(ControlThrowable.class) });

		return new Emit(this, context, mv);
	}

}
