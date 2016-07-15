package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.analysis.BranchInlinerVisitor;
import net.sandius.rembulan.compiler.analysis.SlotAllocInfo;
import net.sandius.rembulan.compiler.analysis.SlotAllocator;
import net.sandius.rembulan.compiler.analysis.TypeInfo;
import net.sandius.rembulan.compiler.analysis.TyperVisitor;
import net.sandius.rembulan.compiler.gen.BytecodeEmitter;
import net.sandius.rembulan.compiler.gen.CompiledClass;
import net.sandius.rembulan.compiler.gen.asm.ASMBytecodeEmitter;
import net.sandius.rembulan.compiler.util.CodeSimplifier;
import net.sandius.rembulan.parser.ParseException;
import net.sandius.rembulan.parser.Parser;
import net.sandius.rembulan.parser.analysis.LabelResolutionTransformer;
import net.sandius.rembulan.parser.analysis.NameResolutionTransformer;
import net.sandius.rembulan.parser.ast.Chunk;
import net.sandius.rembulan.util.Check;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Compiler {

	private static Chunk parse(String sourceText) throws ParseException {
		ByteArrayInputStream bais = new ByteArrayInputStream(sourceText.getBytes());
		Parser parser = new Parser(bais);
		return parser.Chunk();
	}

	private static Module translate(Chunk chunk) {
		// resolve variable names and labels
		chunk = new NameResolutionTransformer().transform(chunk);
		chunk = new LabelResolutionTransformer().transform(chunk);

		// translate into IR
		ModuleBuilder moduleBuilder = new ModuleBuilder();
		IRTranslatorTransformer translator = new IRTranslatorTransformer(moduleBuilder);
		translator.transform(chunk);
		return moduleBuilder.build();
	}

	private Iterable<IRFunc> sortTopologically(Module module) {
		// TODO
		return module.fns();
	}

	private IRFunc insertCPUAccounting(IRFunc fn) {
		CPUAccountingVisitor visitor = new CPUAccountingVisitor(CPUAccountingVisitor.INITIALISE);
		visitor.visit(fn);
		return fn.update(visitor.result());
  	}

	private IRFunc collectCPUAccounting(IRFunc fn) {
		CPUAccountingVisitor visitor = new CPUAccountingVisitor(CPUAccountingVisitor.COLLECT);
		visitor.visit(fn);
		return fn.update(visitor.result());
  	}

	private IRFunc inlineBranches(IRFunc fn, TypeInfo typeInfo) {
		BranchInlinerVisitor visitor = new BranchInlinerVisitor(typeInfo);
		visitor.visit(fn);
		return fn.update(visitor.result());
	}

	private TypeInfo typeInfo(IRFunc fn) {
		TyperVisitor visitor = new TyperVisitor();
		visitor.visit(fn);
		return visitor.valTypes();
	}

	private SlotAllocInfo assignSlots(IRFunc fn) {
		return SlotAllocator.allocateSlots(fn);
	}

	private CompiledClass emitBytecode(IRFunc fn, SlotAllocInfo slots, TypeInfo typeInfo) {
		BytecodeEmitter emitter = new ASMBytecodeEmitter();
		return emitter.emit(fn, slots, typeInfo);
	}

	private IRFunc optimise(IRFunc fn) {
		IRFunc oldFn;

		do {
			oldFn = fn;

			TypeInfo typeInfo = typeInfo(fn);
			fn = collectCPUAccounting(fn);
			fn = inlineBranches(fn, typeInfo);
			fn = fn.update(CodeSimplifier.pruneUnreachableCode(fn.blocks()));
			fn = fn.update(CodeSimplifier.mergeBlocks(fn.blocks()));

		} while (!oldFn.equals(fn));

		return fn;
	}

	private CompiledClass compile(IRFunc fn) {
		fn = insertCPUAccounting(fn);
		fn = optimise(fn);

		SlotAllocInfo slots = assignSlots(fn);
		TypeInfo typeInfo = typeInfo(fn);

		return emitBytecode(fn, slots, typeInfo);
	}

	public CompiledModule compile(String sourceText) throws ParseException {
		Check.notNull(sourceText);
		Chunk ast = parse(sourceText);
		Module translated = translate(ast);

		List<CompiledClass> classes = new ArrayList<>();
		String mainClass = null;
		for (IRFunc fn : sortTopologically(translated)) {
			CompiledClass cc = compile(fn);

			if (fn.id().isRoot()) {
				assert (mainClass == null);
				mainClass = cc.name();
			}
			classes.add(cc);
		}

		if (mainClass == null) {
			throw new IllegalStateException("Module main class not found");
		}

		return new CompiledModule(Collections.unmodifiableList(classes), mainClass);
	}

}
