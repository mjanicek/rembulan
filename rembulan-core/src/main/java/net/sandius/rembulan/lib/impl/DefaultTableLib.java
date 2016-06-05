package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaRuntimeException;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.lib.TableLib;

import java.util.ArrayList;

public class DefaultTableLib extends TableLib {

	@Override
	public Function _concat() {
		return null;  // TODO
	}

	@Override
	public Function _insert() {
		return null;  // TODO
	}

	@Override
	public Function _move() {
		return null;  // TODO
	}

	@Override
	public Function _pack() {
		return Pack.INSTANCE;
	}

	@Override
	public Function _remove() {
		return null;  // TODO
	}

	@Override
	public Function _sort() {
		return null;  // TODO
	}

	@Override
	public Function _unpack() {
		return Unpack.INSTANCE;
	}


	public static class Pack extends LibFunction {

		public static final Pack INSTANCE = new Pack();

		@Override
		protected String name() {
			return "pack";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Table table = context.getState().newTable();

			int n = 0;
			while (args.hasNext()) {
				table.rawset(n + 1, args.nextAny());
				n += 1;
			}
			table.rawset("n", Long.valueOf(n));

			context.getObjectSink().setTo(table);
		}

	}

	public static class Unpack extends LibFunction {

		public static final Unpack INSTANCE = new Unpack();

		@Override
		protected String name() {
			return "unpack";
		}

		private static class SuspendedState {

			public final int state;

			public final Table table;
			public final int i;
			public final int j;
			public final int k;
			public final ArrayList<Object> result;

			public SuspendedState(int state, Table table, int i, int j, int k, ArrayList<Object> result) {
				this.state = state;
				this.table = table;
				this.i = i;
				this.j = j;
				this.k = k;
				this.result = result;
			}

		}

		private void run(ExecutionContext context, int state, Table table, int i, int j, int k, ArrayList<Object> result) throws ControlThrowable {
			try {
				switch (state) {
					case 0:
						state = 1;
						Dispatch.len(context, table);
					case 1: {
						Object o = context.getObjectSink()._0();
						Long l = Conversions.integerValueOf(o);
						if (l == null) {
							throw new LuaRuntimeException("object length is not an integer");
						}
						j = (int) l.longValue();
					}
					case 2:
						// k is the index running from i to j (inclusive)

						if (Metatables.getMetamethod(context.getState(), Metatables.MT_INDEX, table) == null) {
							// no __index metamethod, we can use rawget

							ArrayList<Object> r = new ArrayList<>();
							for (k = i; k <= j; k++) {
								r.add(table.rawget(k));
							}
							context.getObjectSink().setToArray(r.toArray(new Object[r.size()]));
							return;
						}
						else {
							// table has the __index metamethod, need to go through Dispatch
							// and be ready to be suspended

							if (i <= j) {
								k = i;
								state = 3;
								result = new ArrayList<>();  // allocate the result accumulator
								Dispatch.index(context, table, k++);

								// fall-through to state == 3
							}
							else {
								// interval empty, we're done
								context.getObjectSink().reset();
								return;
							}
						}

					case 3: {
						while (true) {
							// k now points to the *next* item to retrieve; we've just processed
							// (k - 1), need to add it to the results

							Object o = context.getObjectSink()._0();
							result.add(o);

							// we may now continue
							if (k <= j) {
								Dispatch.index(context, table, k++);
							}
							else {
								break;
							}
						}

						assert (k > j);

						// we're done!
						context.getObjectSink().setToArray(result.toArray(new Object[result.size()]));
						return;
					}


					default:
						throw new IllegalStateException("Illegal state: " + state);
				}
			}
			catch (ControlThrowable ct) {
				ct.push(this, new SuspendedState(state, table, i, j, k, result));
				throw ct;
			}

		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Table table = args.nextTable();
			final int i = args.optNextInt(1);
			Integer maybeJ = args.hasNext() ? args.nextInt() : null;

			if (maybeJ == null) {
				// no explicit 'j' argument
				run(context, 0, table, i, 0, 0, null);
			}
			else {
				// explicit 'j', we can skip the computation of #len
				int j = maybeJ.intValue();
				run(context, 2, table, i, j, 0, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			SuspendedState ss = (SuspendedState) suspendedState;
			run(context, ss.state, ss.table, ss.i, ss.j, ss.k, ss.result);
		}

	}


}
