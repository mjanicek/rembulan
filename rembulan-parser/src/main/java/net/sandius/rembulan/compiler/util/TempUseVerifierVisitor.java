package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.compiler.analysis.AbstractUseDefVisitor;
import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.util.Check;

import java.util.HashSet;
import java.util.Set;

// A visitor that checks that each temp is assigned to before used, and that no temp
// is assigned to more than once.
@Deprecated
public class TempUseVerifierVisitor extends AbstractUseDefVisitor {

	private final Set<Val> assignedTo;
	private final Set<Val> used;

	public TempUseVerifierVisitor() {
		this.assignedTo = new HashSet<>();
		this.used = new HashSet<>();
	}

	@Override
	protected void def(Val v) {
		Check.notNull(v);
		if (!assignedTo.add(v)) {
			throw new IllegalStateException(v.toString() + " assigned to more than once");
		}
	}

	@Override
	protected void use(Val v) {
		Check.notNull(v);
		if (!assignedTo.contains(v)) {
			throw new IllegalStateException(v.toString() + " used before assigned to");
		}
		used.add(v);
	}

	@Override
	protected void def(PhiVal v) {
		// TODO
	}

	@Override
	protected void use(PhiVal v) {
		// TODO
	}

	@Override
	protected void def(Var v) {
		// TODO
	}

	@Override
	protected void use(Var v) {
		// TODO
	}

	@Override
	protected void def(UpVar uv) {
		// TODO
	}

	@Override
	protected void use(UpVar uv) {
		// TODO
	}

}
