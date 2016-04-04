package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.Emit;
import net.sandius.rembulan.compiler.gen.block.Entry;
import net.sandius.rembulan.compiler.gen.block.Node;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.compiler.types.TypeSeq;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Invokable;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.HashSet;

import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.V1_7;
import static sun.tools.java.RuntimeConstants.ACC_PUBLIC;
import static sun.tools.java.RuntimeConstants.ACC_SUPER;

public class CompilationUnit {

	public final Prototype prototype;
	public final String name;

	public final PrototypeContext ctx;

	private FunctionCode generic;

	public CompilationUnit(Prototype prototype, String name, CompilationContext ctx) {
		this.prototype = Check.notNull(prototype);
		this.name = name;
		this.ctx = new PrototypeContext(Check.notNull(ctx), prototype);

		this.generic = null;
	}

	public String name() {
		return name;
	}

	public FunctionCode generic() {
		return generic;
	}

	public TypeSeq genericParameters() {
		return FunctionCode.genericParameterTypes(prototype.getNumberOfParameters(), prototype.isVararg());
	}

	public Entry makeNodes(TypeSeq params) {
		IntVector code = prototype.getCode();
		Target[] targets = new Target[code.length()];
		for (int pc = 0; pc < targets.length; pc++) {
			targets[pc] = new Target(Integer.toString(pc + 1));
		}

		ReadOnlyArray<Target> pcLabels = ReadOnlyArray.wrap(targets);

		LuaInstructionToNodeTranslator translator = new LuaInstructionToNodeTranslator(prototype, pcLabels, ctx);

		for (int pc = 0; pc < pcLabels.size(); pc++) {
			translator.translate(pc);
		}

		String suffix = params.toString();

		return new Entry("main_" + suffix, params, prototype.getMaximumStackSize(), pcLabels.get(0));
	}

	public FunctionCode makeCompiledPrototype(TypeSeq params) {
		FunctionCode cp = new FunctionCode(prototype, params);
		cp.callEntry = makeNodes(params);
		cp.returnTypes = TypeSeq.vararg();
		cp.resumePoints = new HashSet<>();
		return cp;
	}

	public void initGeneric() {
		this.generic = makeCompiledPrototype(genericParameters());
	}

	public CompiledClass toCompiledClass() {
		Iterable<Node> topoSorted = generic.sortTopologically();

		int i = 0;

		ClassVisitor cv = new TraceClassVisitor(new PrintWriter(System.out));

		Type thisType = Type.getType(ctx.className());

		cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, thisType.getInternalName(), null, Type.getInternalName(Invokable.class), null);
		cv.visitSource(prototype.getShortSource(), null);

		Type methodType = Type.getMethodType(
				Type.VOID_TYPE,
				Type.INT_TYPE
		);

		MethodVisitor mv = cv.visitMethod(ACC_PROTECTED, "resume", methodType.getDescriptor(),
				null,
				new String[] { Type.getInternalName(ControlThrowable.class) });

		Emit e = new Emit(ctx, mv);

		for (Node n : topoSorted) {
//			System.out.println("// " + i + ": " + n.toString());
			n.emit(e);
			i += 1;
		}

		mv.visitEnd();
		cv.visitEnd();

		return null;  // TODO
	}

}
