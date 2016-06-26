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
	private final Blocks blocks;
	private final List<FunctionId> nested;

	public IRFunc(FunctionId id, List<Var> params, List<UpVar> upvals, Blocks blocks, List<FunctionId> nested) {
		this.id = Check.notNull(id);
		this.params = Check.notNull(params);
		this.upvals = Check.notNull(upvals);
		this.blocks = Check.notNull(blocks);
		this.nested = Check.notNull(nested);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IRFunc that = (IRFunc) o;
		return id.equals(that.id)
				&& params.equals(that.params)
				&& upvals.equals(that.upvals)
				&& blocks.equals(that.blocks)
				&& nested.equals(that.nested);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, params, blocks, nested);
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

	public Blocks blocks() {
		return blocks;
	}

	public List<FunctionId> nested() {
		return nested;
	}

	public IRFunc update(Blocks blocks) {
		if (this.blocks.equals(blocks)) {
			return this;
		}
		else {
			return new IRFunc(id, params, upvals, blocks, nested);
		}
	}

}
