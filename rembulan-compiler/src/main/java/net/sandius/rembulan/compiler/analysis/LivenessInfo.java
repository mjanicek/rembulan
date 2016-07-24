package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.ir.AbstractVal;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.Var;
import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class LivenessInfo {

	private final Map<IRNode, Entry> entries;
	
	public LivenessInfo(Map<IRNode, Entry> entries) {
		this.entries = Check.notNull(entries);
	}

	public static class Entry {

		private final Set<Var> var_in;
		private final Set<Var> var_out;
		private final Set<AbstractVal> val_in;
		private final Set<AbstractVal> val_out;

		Entry(Set<Var> var_in, Set<Var> var_out, Set<AbstractVal> val_in, Set<AbstractVal> val_out) {
			this.var_in = Check.notNull(var_in);
			this.var_out = Check.notNull(var_out);
			this.val_in = Check.notNull(val_in);
			this.val_out = Check.notNull(val_out);
		}

		public Entry immutableCopy() {
			return new Entry(
					Collections.unmodifiableSet(new HashSet<>(var_in)),
					Collections.unmodifiableSet(new HashSet<>(var_out)),
					Collections.unmodifiableSet(new HashSet<>(val_in)),
					Collections.unmodifiableSet(new HashSet<>(val_out)));
		}

		public Set<Var> inVar() {
			return var_in;
		}

		public Set<Var> outVar() {
			return var_out;
		}

		public Set<AbstractVal> inVal() {
			return val_in;
		}

		public Set<AbstractVal> outVal() {
			return val_out;
		}

	}

	public Entry entry(IRNode node) {
		Check.notNull(node);
		Entry e = entries.get(node);
		if (e == null) {
			throw new NoSuchElementException("No liveness information for " + node);
		}
		else {
			return e;
		}
	}

	public Iterable<Var> liveInVars(IRNode node) {
		return entry(node).inVar();
	}

	public Iterable<Var> liveOutVars(IRNode node) {
		return entry(node).outVar();
	}

	public Iterable<AbstractVal> liveInVals(IRNode node) {
		return entry(node).inVal();
	}

	public Iterable<AbstractVal> liveOutVals(IRNode node) {
		return entry(node).outVal();
	}

}
