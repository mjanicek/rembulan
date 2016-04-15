package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.asm.ClassEmitter;
import net.sandius.rembulan.compiler.gen.asm.CodeEmitter;
import net.sandius.rembulan.compiler.gen.block.Entry;
import net.sandius.rembulan.compiler.gen.block.Node;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.compiler.types.TypeSeq;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.ByteVector;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.HashSet;

public class CompilationUnit {

	public final Prototype prototype;
	public final String name;

	public final PrototypeContext ctx;

	private FunctionCode generic;

	public CompilationUnit(Prototype prototype, Prototype parent, String name, CompilationContext ctx) {
		this.prototype = Check.notNull(prototype);
		this.name = name;
		this.ctx = new PrototypeContext(Check.notNull(ctx), prototype, parent);

		this.generic = null;
	}

	public String name() {
		return name;
	}

	public FunctionCode generic() {
		return generic;
	}

	public TypeSeq fixedGenericParameters() {
		return FunctionCode.genericParameterTypes(prototype.getNumberOfParameters(), false);
	}

	public Entry makeNodes(TypeSeq fixedParams) {
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

		String suffix = fixedParams.toString();

		return new Entry("main_" + suffix, fixedParams, prototype.getMaximumStackSize(), pcLabels.get(0));
	}

	public FunctionCode makeCompiledPrototype(TypeSeq fixedParams) {
		FunctionCode cp = new FunctionCode(prototype);
		cp.callEntry = makeNodes(fixedParams);
		cp.returnTypes = TypeSeq.vararg();
		cp.resumePoints = new HashSet<>();
		return cp;
	}

	public void initGeneric() {
		this.generic = makeCompiledPrototype(fixedGenericParameters());
	}

	public CompiledClass toCompiledClass() {
		Iterable<Node> topoSorted = generic.sortTopologically();

		int numFixedParams = generic.numOfFixedParameters();
		boolean isVararg = generic.isVararg();

		ClassEmitter classEmitter = new ClassEmitter(ctx, numFixedParams, isVararg);

		classEmitter.begin();

		CodeEmitter codeEmitter = classEmitter.code();

		codeEmitter.begin();
		for (Node n : topoSorted) {
			n.emit(codeEmitter.codeVisitor());
		}
		codeEmitter.end();

		classEmitter.end();

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classEmitter.accept(writer);
		byte[] bytes = writer.toByteArray();

		// verify bytecode
		ClassReader reader = new ClassReader(bytes);
		ClassVisitor tracer = new TraceClassVisitor(new PrintWriter(System.out));
		ClassVisitor checker = new CheckClassAdapter(tracer, true);
		reader.accept(checker, 0);

		return new CompiledClass(ctx.className(), ByteVector.wrap(bytes));
	}

}
