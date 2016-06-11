package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.parser.util.Util;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FunctionParams {

	private final List<Name> params;
	private final boolean vararg;

	public FunctionParams(List<Name> params, boolean vararg) {
		this.params = Check.notNull(params);
		this.vararg = vararg;
	}

	public static FunctionParams empty() {
		List<Name> l = Collections.emptyList();
		return new FunctionParams(l, false);
	}

	public List<Name> names() {
		return params;
	}

	public boolean isVararg() {
		return vararg;
	}

	public FunctionParams prepend(Name n) {
		Check.notNull(n);
		List<Name> pp = new ArrayList<>();
		pp.add(n);
		pp.addAll(params);
		return new FunctionParams(Collections.unmodifiableList(pp), vararg);
	}

	@Override
	public String toString() {
		return "(fnparam [" + Util.listToString(params, ", ") + "] vararg=" + vararg + ")";
	}

}
