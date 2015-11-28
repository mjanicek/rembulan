package net.sandius.rembulan.core;

import net.sandius.rembulan.gen.LuaBytecodeMethodVisitor;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;
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
	private final Map<String, Prototype> installed;
	private int index;

	public PrototypeClassLoader(String rootName) {
		Check.notNull(rootName);

		this.rootName = rootName;
		this.installed = new HashMap<>();
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

		ClassWriter _cw = new ClassWriter(0); //ClassWriter.COMPUTE_FRAMES);
		ClassVisitor cw = new TraceClassVisitor(_cw, new PrintWriter(System.err));

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
				prototype.getCode().length(),
				prototype.getMaximumStackSize()
		);

		lmv.begin();
		for (int i = 0; i < code.length(); i++) {
			lmv.atPc(i, prototype.getLineAtPC(i));
			lmv.instruction(code.get(i));
		}
		lmv.end();

		cw.visitEnd();

		return _cw.toByteArray();
	}

	private static void printClassStructure(byte[] classBytes) {
		// print the class structure for debugging
		ClassReader reader = new ClassReader(classBytes);
		ClassVisitor cv = new TraceClassVisitor(new PrintWriter(System.err));
		reader.accept(cv, ClassReader.EXPAND_FRAMES);
	}

	private String install(String className, Prototype prototype) {
		System.err.println("Installing " + PrototypePrinter.pseudoAddr(prototype) + " as " + className);
		installed.put(className, prototype);

		ReadOnlyArray<Prototype> nested = prototype.getNestedPrototypes();
		for (int i = 0; i < nested.size(); i++) {
			install(className + "$" + (i + 1), nested.get(i));
		}

		return className;
	}

	public String install(Prototype prototype) {
		return install(rootName + "$" + (index++), prototype);
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		Prototype proto = installed.get(name);
		if (proto != null) {
			System.err.println();
			System.err.println("COMPILING");
			System.err.println("---------");
			byte[] javaBytes = compile(proto, name);

			System.err.println();
			System.err.println("FINISHED");
			System.err.println("--------");
			printClassStructure(javaBytes);
			return defineCompiledLuaFunction(name, javaBytes);
		}
		else {
			throw new ClassNotFoundException(name);
		}
	}

}
