package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.ir.AbstractVal;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.Var;
import net.sandius.rembulan.util.Check;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

class LivenessInfo {

	private final Map<IRNode, Set<Var>> inVar;
	private final Map<IRNode, Set<AbstractVal>> inVal;

	LivenessInfo(Map<IRNode, Set<Var>> inVar, Map<IRNode, Set<AbstractVal>> inVal) {
		this.inVar = Check.notNull(inVar);
		this.inVal = Check.notNull(inVal);
	}

	public Iterable<Var> inVar(IRNode node) {
		Check.notNull(node);
		Set<Var> s = inVar.get(node);
		if (s == null) {
			throw new NoSuchElementException("No live-in information for " + node);
		}
		else {
			return s;
		}
	}

	public Iterable<AbstractVal> inVal(IRNode node) {
		Check.notNull(node);
		Set<AbstractVal> s = inVal.get(node);
		if (s == null) {
			throw new NoSuchElementException("No live-in information for " + node);
		}
		else {
			return s;
		}
	}

}
