package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.analysis.DependencyAnalyser;
import net.sandius.rembulan.compiler.analysis.DependencyInfo;
import net.sandius.rembulan.compiler.analysis.LivenessAnalyser;
import net.sandius.rembulan.compiler.analysis.LivenessInfo;
import net.sandius.rembulan.compiler.analysis.SlotAllocInfo;
import net.sandius.rembulan.compiler.analysis.SlotAllocator;
import net.sandius.rembulan.compiler.analysis.TypeInfo;
import net.sandius.rembulan.compiler.analysis.Typer;
import net.sandius.rembulan.compiler.gen.BytecodeEmitter;
import net.sandius.rembulan.compiler.gen.ClassNameTranslator;
import net.sandius.rembulan.compiler.gen.CompiledClass;
import net.sandius.rembulan.compiler.gen.SuffixingClassNameTranslator;
import net.sandius.rembulan.compiler.gen.asm.ASMBytecodeEmitter;
import net.sandius.rembulan.compiler.tf.BranchInliner;
import net.sandius.rembulan.compiler.tf.CPUAccounter;
import net.sandius.rembulan.compiler.tf.CodeSimplifier;
import net.sandius.rembulan.compiler.tf.ConstFolder;
import net.sandius.rembulan.compiler.tf.LivenessPruner;
import net.sandius.rembulan.parser.ParseException;
import net.sandius.rembulan.parser.Parser;
import net.sandius.rembulan.parser.analysis.NameResolver;
import net.sandius.rembulan.parser.ast.Chunk;
import net.sandius.rembulan.util.ByteVector;
import net.sandius.rembulan.util.Check;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Compiler {

	public enum CPUAccountingMode {
		NO_CPU_ACCOUNTING,
		IN_EVERY_BASIC_BLOCK
	}

	public static final CPUAccountingMode DEFAULT_CPU_ACCOUNTING_MODE = CPUAccountingMode.IN_EVERY_BASIC_BLOCK;

	private CPUAccountingMode cpuAccountingMode;

	public Compiler(CPUAccountingMode cpuAccountingMode) {
		this.cpuAccountingMode = Check.notNull(cpuAccountingMode);
	}

	public Compiler() {
		this(DEFAULT_CPU_ACCOUNTING_MODE);
	}

	public void setCPUAccountingMode(CPUAccountingMode mode) {
		cpuAccountingMode = Check.notNull(mode);
	}

	public CPUAccountingMode getCPUAccountingMode() {
		return cpuAccountingMode;
	}

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

	private IRFunc optimise(IRFunc fn) {
		IRFunc oldFn;

		do {
			oldFn = fn;

			TypeInfo typeInfo = Typer.analyseTypes(fn);

			fn = CPUAccounter.collectCPUAccounting(fn);
			fn = BranchInliner.inlineBranches(fn, typeInfo);
			fn = ConstFolder.replaceConstOperations(fn, typeInfo);

			LivenessInfo liveness = LivenessAnalyser.computeLiveness(fn);

			fn = LivenessPruner.pruneDeadCode(fn, typeInfo, liveness);
			fn = CodeSimplifier.pruneUnreachableCode(fn);
			fn = CodeSimplifier.mergeBlocks(fn);

		} while (!oldFn.equals(fn));

		return fn;
	}

	private static class ProcessedFunc {

		public final IRFunc fn;
		public final SlotAllocInfo slots;
		public final TypeInfo types;
		public final DependencyInfo deps;

		private ProcessedFunc(IRFunc fn, SlotAllocInfo slots, TypeInfo types, DependencyInfo deps) {
			this.fn = Check.notNull(fn);
			this.slots = Check.notNull(slots);
			this.types = Check.notNull(types);
			this.deps = Check.notNull(deps);
		}

	}

	ProcessedFunc processFunction(IRFunc fn) {
		fn = CPUAccounter.insertCPUAccounting(fn);
		fn = optimise(fn);

		SlotAllocInfo slots = SlotAllocator.allocateSlots(fn);
		TypeInfo types = Typer.analyseTypes(fn);
		DependencyInfo deps = DependencyAnalyser.analyse(fn);

		return new ProcessedFunc(fn, slots, types, deps);
	}

	private Iterable<ProcessedFunc> processModule(Module m) {
		Map<FunctionId, ProcessedFunc> pfs = new HashMap<>();

		for (IRFunc fn : sortTopologically(m)) {
			ProcessedFunc pf = processFunction(fn);
			pfs.put(fn.id(), pf);
		}

		ProcessedFunc main = pfs.get(FunctionId.root());
		assert (main != null);

		Set<ProcessedFunc> result = new HashSet<>();
		Stack<ProcessedFunc> open = new Stack<>();

		// only add functions reachable from main
		open.add(main);
		while (!open.isEmpty()) {
			ProcessedFunc pf = open.pop();
			if (!result.contains(pf)) {
				result.add(pf);
				for (FunctionId id : pf.deps.nestedRefs()) {
					open.push(pfs.get(id));
				}
			}
		}

		return result;
	}

	private CompiledClass compileFunction(ProcessedFunc pf, String sourceFileName, String rootClassName) {
		ClassNameTranslator classNameTranslator = new SuffixingClassNameTranslator(rootClassName);
		BytecodeEmitter emitter = new ASMBytecodeEmitter(
				pf.fn, pf.slots, pf.types, pf.deps,
				cpuAccountingMode, classNameTranslator,
				sourceFileName);
		return emitter.emit();
	}

	public CompiledModule compile(String sourceText, String sourceFileName, String rootClassName) throws ParseException {
		Check.notNull(sourceText);
		Chunk ast = parse(sourceText);
		Module module = translate(ast);

		Iterable<ProcessedFunc> pfs = processModule(module);

		Map<String, ByteVector> classMap = new HashMap<>();
		String mainClass = null;
		for (ProcessedFunc pf : pfs) {
			CompiledClass cc = compileFunction(pf, sourceFileName, rootClassName);

			if (pf.fn.id().isRoot()) {
				assert (mainClass == null);
				mainClass = cc.name();
			}

			classMap.put(cc.name(), cc.bytes());
		}

		if (mainClass == null) {
			throw new IllegalStateException("Module main class not found");
		}

		return new CompiledModule(Collections.unmodifiableMap(classMap), mainClass);
	}

}
