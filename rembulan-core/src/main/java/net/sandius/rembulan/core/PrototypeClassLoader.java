package net.sandius.rembulan.core;

import net.sandius.rembulan.gen.LuaBytecodeMethodVisitor;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.asm.ASMUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
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

	protected byte[] compile(final Prototype prototype, final String className) {
		Check.notNull(prototype);
		Check.notNull(className);

		final Type thisType = ASMUtils.typeForClassName(className);

		ClassWriter cw = new ClassWriter(0); //ClassWriter.COMPUTE_FRAMES);

		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, thisType.getInternalName(), null, Type.getInternalName(Function.class), null);
		cw.visitSource(prototype.getSource(), null);

		// constructor
		LuaBytecodeMethodVisitor.emitConstructor(cw, thisType);

		// function body
		IntVector code = prototype.getCode();

		LuaBytecodeMethodVisitor lmv = new LuaBytecodeMethodVisitor(
				cw,
				thisType,
				prototype.getConstants(),
				prototype.getNestedPrototypes(),
				new PrototypeToClassMap() {
					@Override
					public String classNameFor(int idx) {
						return className + "$" + (idx + 1);
					}
				},
				prototype.getCode().length() - 1,  // ignoring the last return instruction
				prototype.getMaximumStackSize()
		);

		lmv.begin();
		for (int i = 0; i < code.length() - 1; i++) {  // ignoring the last return instruction
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

			{
				// print the class structure for debugging
				ClassReader reader = new ClassReader(javaClassBytes);
				ClassVisitor cv = new TraceClassVisitor(new PrintWriter(System.err));
				reader.accept(cv, 0);
			}

			// load the compiled class
			cp = defineCompiledLuaFunction(name, javaClassBytes);

			compiled.put(prototype, cp);
			return cp;
		}
	}

}
