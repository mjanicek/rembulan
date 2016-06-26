package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.analysis.BranchInlinerVisitor;
import net.sandius.rembulan.compiler.analysis.SlotAllocInfo;
import net.sandius.rembulan.compiler.analysis.TypeInfo;
import net.sandius.rembulan.compiler.analysis.TyperVisitor;
import net.sandius.rembulan.compiler.gen.CompiledClass;
import net.sandius.rembulan.compiler.util.BlocksSimplifier;
import net.sandius.rembulan.parser.ParseException;
import net.sandius.rembulan.parser.Parser;
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
		// resolve names
		Chunk resolved = new NameResolutionTransformer().transform(chunk);

		// translate into IR
		ModuleBuilder moduleBuilder = new ModuleBuilder();
		IRTranslatorTransformer translator = new IRTranslatorTransformer(moduleBuilder);
		translator.transform(resolved);
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
		throw new UnsupportedOperationException();  // TODO
	}

	private CompiledClass emitBytecode(IRFunc fn, SlotAllocInfo slots, TypeInfo typeInfo) {
		throw new UnsupportedOperationException();  // TODO
	}

	private IRFunc optimise(IRFunc fn) {
		IRFunc oldFn;

		do {
			oldFn = fn;

			TypeInfo typeInfo = typeInfo(fn);
			fn = collectCPUAccounting(fn);
			fn = inlineBranches(fn, typeInfo);
			fn = fn.update(BlocksSimplifier.filterUnreachableBlocks(fn.blocks()));
			fn = fn.update(BlocksSimplifier.mergeBlocks(fn.blocks()));

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
