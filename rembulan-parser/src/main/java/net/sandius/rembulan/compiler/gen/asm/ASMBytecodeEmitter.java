package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.analysis.DependencyInfo;
import net.sandius.rembulan.compiler.analysis.SlotAllocInfo;
import net.sandius.rembulan.compiler.analysis.TypeInfo;
import net.sandius.rembulan.compiler.gen.BytecodeEmitter;
import net.sandius.rembulan.compiler.gen.CompiledClass;

public class ASMBytecodeEmitter extends BytecodeEmitter {

	@Override
	public CompiledClass emit(IRFunc fn, SlotAllocInfo slots, TypeInfo typeInfo, DependencyInfo deps) {
		throw new UnsupportedOperationException();  // TODO
	}

}
