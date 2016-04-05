package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.ClassEmit;
import net.sandius.rembulan.compiler.gen.block.Emit;
import net.sandius.rembulan.compiler.gen.block.Entry;
import net.sandius.rembulan.compiler.gen.block.Node;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.compiler.types.TypeSeq;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.HashSet;

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

		ClassVisitor cv = new TraceClassVisitor(new PrintWriter(System.out));

		ClassEmit ce = new ClassEmit(ctx, cv);

		ce._begin();

		Emit e = ce._code_emit();

		e._begin();
		for (Node n : topoSorted) {
			n.emit(e);
		}
		e._end();

		ce._end();

		return null;  // TODO
	}

}
