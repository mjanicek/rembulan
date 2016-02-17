package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.Entry;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Unit {

	public final Prototype prototype;

	private CompiledPrototype generic;

	public Unit(Prototype prototype) {
		this.prototype = Objects.requireNonNull(prototype);

		this.generic = null;
	}

	public CompiledPrototype generic() {
		return generic;
	}

	public TypeSeq genericParameters() {
		Type[] types = new Type[prototype.getNumberOfParameters()];
		for (int i = 0; i < types.length; i++) {
			types[i] = Type.ANY;
		}
		return new TypeSeq(ReadOnlyArray.wrap(types), prototype.isVararg());
	}

	public Entry makeGenericNodes() {
		return makeNodes(genericParameters());
	}

	public Entry makeNodes(TypeSeq params) {
		IntVector code = prototype.getCode();
		Target[] targets = new Target[code.length()];
		for (int pc = 0; pc < targets.length; pc++) {
			targets[pc] = new Target(Integer.toString(pc + 1));
		}

		ReadOnlyArray<Target> pcLabels = ReadOnlyArray.wrap(targets);

		LuaInstructionToNodeTranslator translator = new LuaInstructionToNodeTranslator(prototype, pcLabels);

		for (int pc = 0; pc < pcLabels.size(); pc++) {
			translator.translate(pc);
		}

		String suffix = params.toString();

		return new Entry("main_" + suffix, params, prototype.getMaximumStackSize(), pcLabels.get(0));
	}

	public CompiledPrototype makeCompiledPrototype(TypeSeq params) {
		CompiledPrototype cp = new CompiledPrototype(prototype, params);
		cp.callEntry = makeNodes(params);
		cp.returnType = TypeSeq.vararg();
		cp.resumePoints = new HashSet<>();
		return cp;
	}

	public void setGeneric(CompiledPrototype cp) {
		this.generic = cp;
	}

}
