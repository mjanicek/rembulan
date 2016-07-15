package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.tf.BranchInliner;
import net.sandius.rembulan.compiler.analysis.SlotAllocInfo;
import net.sandius.rembulan.compiler.analysis.SlotAllocator;
import net.sandius.rembulan.compiler.analysis.TypeInfo;
import net.sandius.rembulan.compiler.analysis.Typer;
import net.sandius.rembulan.compiler.gen.BytecodeEmitter;
import net.sandius.rembulan.compiler.gen.CompiledClass;
import net.sandius.rembulan.compiler.gen.asm.ASMBytecodeEmitter;
import net.sandius.rembulan.compiler.tf.CPUAccounter;
import net.sandius.rembulan.compiler.tf.CodeSimplifier;
import net.sandius.rembulan.parser.ParseException;
import net.sandius.rembulan.parser.Parser;
import net.sandius.rembulan.parser.analysis.NameResolver;
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
		chunk = NameResolver.resolveNames(chunk);
		return IRTranslator.translate(chunk);
	}

	private Iterable<IRFunc> sortTopologically(Module module) {
		// TODO
		return module.fns();
	}

	private CompiledClass emitBytecode(IRFunc fn, SlotAllocInfo slots, TypeInfo typeInfo) {
		BytecodeEmitter emitter = new ASMBytecodeEmitter();
		return emitter.emit(fn, slots, typeInfo);
	}

	private IRFunc optimise(IRFunc fn) {
		IRFunc oldFn;

		do {
			oldFn = fn;

			TypeInfo typeInfo = Typer.analyseTypes(fn);
			fn = CPUAccounter.collectCPUAccounting(fn);
			fn = BranchInliner.inlineBranches(fn, typeInfo);
			fn = CodeSimplifier.pruneUnreachableCode(fn);
			fn = CodeSimplifier.mergeBlocks(fn);

		} while (!oldFn.equals(fn));

		return fn;
	}

	private CompiledClass compile(IRFunc fn) {
		fn = CPUAccounter.insertCPUAccounting(fn);
		fn = optimise(fn);

		SlotAllocInfo slots = SlotAllocator.allocateSlots(fn);
		TypeInfo typeInfo = Typer.analyseTypes(fn);

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
