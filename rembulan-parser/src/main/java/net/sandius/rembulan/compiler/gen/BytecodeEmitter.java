package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.analysis.DependencyInfo;
import net.sandius.rembulan.compiler.analysis.SlotAllocInfo;
import net.sandius.rembulan.compiler.analysis.TypeInfo;

public abstract class BytecodeEmitter {

	public abstract CompiledClass emit(IRFunc fn, SlotAllocInfo slots, TypeInfo typeInfo, DependencyInfo deps);

}
