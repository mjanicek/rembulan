package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.UpVar;
import net.sandius.rembulan.compiler.ir.Var;
import net.sandius.rembulan.util.Check;

import java.util.List;
import java.util.Objects;

public class IRFunc {

	private final FunctionId id;
	private final List<Var> params;
	private final List<UpVar> upvals;
	private final Code code;

	public IRFunc(FunctionId id, List<Var> params, List<UpVar> upvals, Code code) {
		this.id = Check.notNull(id);
		this.params = Check.notNull(params);
		this.upvals = Check.notNull(upvals);
		this.code = Check.notNull(code);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IRFunc that = (IRFunc) o;
		return id.equals(that.id)
				&& params.equals(that.params)
				&& upvals.equals(that.upvals)
				&& code.equals(that.code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, params, code);
	}

	public FunctionId id() {
		return id;
	}

	public List<Var> params() {
		return params;
	}

	public List<UpVar> upvals() {
		return upvals;
	}

	public Code blocks() {
		return code;
	}

	public IRFunc update(Code code) {
		if (this.code.equals(code)) {
			return this;
		}
		else {
			return new IRFunc(id, params, upvals, code);
		}
	}

}
