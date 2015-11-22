package net.sandius.rembulan.core;

import net.sandius.rembulan.gen.LuaBytecodeMethodVisitor;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.asm.ASMUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.V1_7;

public class PrototypeClassLoader extends ClassLoader {

	private final String rootName;
	private final Map<Prototype, Class<Function>> compiled;
	private int index;

	public PrototypeClassLoader(String rootName) {
		Check.notNull(rootName);

		this.rootName = rootName;
		this.compiled = new HashMap<>();
		this.index = 1;
	}

	private Class<Function> defineCompiledLuaFunction(String name, byte[] data) {
		Class<?> cl = defineClass(name, data, 0, data.length);

		assert Function.class.isAssignableFrom(cl);

		// FIXME: unchecked, uncheckable cast!
		return (Class<Function>) cl;
	}

	protected byte[] compile(Prototype prototype, String className) {
		Check.notNull(prototype);
		Check.notNull(className);

		Type thisType = ASMUtils.typeForClassName(className);

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, thisType.getInternalName(), null, Type.getInternalName(Function.class), null);
		cw.visitSource(prototype.getSource(), null);

		// constructor
		LuaBytecodeMethodVisitor.emitConstructor(cw, thisType);

		// function body
		IntVector code = prototype.getCode();

		LuaBytecodeMethodVisitor lmv = new LuaBytecodeMethodVisitor(cw, thisType, prototype.getConstants(), prototype.getCode().length(), prototype.getMaximumStackSize());
		lmv.begin();
		for (int i = 0; i < code.length(); i++) {
			lmv.atPc(i, prototype.getLineAtPC(i));
			lmv.instruction(code.get(i));
		}
		lmv.end();

		cw.visitEnd();

		return cw.toByteArray();
	}

	public Class<Function> classForPrototype(Prototype prototype) {
		Check.notNull(prototype);

		Class<Function> cp = compiled.get(prototype);
		if (cp != null) {
			return cp;
		}
		else {
			String name = rootName + "$" + (index++);

			System.err.println("Compiling prototype " + PrototypePrinter.pseudoAddr(prototype) + " to " + name + "...");

			// compile prototype into Java bytecode
			byte[] javaClassBytes = compile(prototype, name);

			// load the compiled class
			cp = defineCompiledLuaFunction(name, javaClassBytes);

			compiled.put(prototype, cp);
			return cp;
		}
	}

}
