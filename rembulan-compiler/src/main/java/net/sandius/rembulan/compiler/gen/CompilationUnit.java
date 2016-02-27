package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.Entry;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.compiler.types.TypeSeq;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.HashSet;
import java.util.Map;

public class CompilationUnit {

	public final Prototype prototype;
	public final String name;

	private CompiledPrototype generic;

	public CompilationUnit(Prototype prototype, String name) {
		this.prototype = Check.notNull(prototype);
		this.name = name;

		this.generic = null;
	}

	public String name() {
		return name;
	}

	public CompiledPrototype generic() {
		return generic;
	}

	public TypeSeq genericParameters() {
		Type[] types = new Type[prototype.getNumberOfParameters()];
		for (int i = 0; i < types.length; i++) {
			types[i] = LuaTypes.DYNAMIC;
		}
		return new TypeSeq(ReadOnlyArray.wrap(types), prototype.isVararg());
	}

	public Entry makeNodes(TypeSeq params, Map<Prototype, CompilationUnit> units) {
		IntVector code = prototype.getCode();
		Target[] targets = new Target[code.length()];
		for (int pc = 0; pc < targets.length; pc++) {
			targets[pc] = new Target(Integer.toString(pc + 1));
		}

		ReadOnlyArray<Target> pcLabels = ReadOnlyArray.wrap(targets);

		LuaInstructionToNodeTranslator translator = new LuaInstructionToNodeTranslator(prototype, pcLabels, units);

		for (int pc = 0; pc < pcLabels.size(); pc++) {
			translator.translate(pc);
		}

		String suffix = params.toString();

		return new Entry("main_" + suffix, params, prototype.getMaximumStackSize(), pcLabels.get(0));
	}

	public CompiledPrototype makeCompiledPrototype(TypeSeq params, Map<Prototype, CompilationUnit> units) {
		CompiledPrototype cp = new CompiledPrototype(prototype, params);
		cp.callEntry = makeNodes(params, units);
		cp.returnType = TypeSeq.vararg();
		cp.resumePoints = new HashSet<>();
		return cp;
	}

	public void initGeneric(Map<Prototype, CompilationUnit> units) {
		this.generic = makeCompiledPrototype(genericParameters(), units);
	}

}
