/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaRuntimeException;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.TableLib;

import java.util.ArrayList;

public class DefaultTableLib extends TableLib {

	private final Function _concat;
	private final Function _insert;
	private final Function _move;
	private final Function _remove;
	private final Function _sort;

	public DefaultTableLib() {
		this._concat = new UnimplementedFunction("table.concat");  // TODO
		this._insert = new UnimplementedFunction("table.insert");  // TODO
		this._move = new UnimplementedFunction("table.move");  // TODO
		this._remove = new UnimplementedFunction("table.remove");  // TODO
		this._sort = new UnimplementedFunction("table.sort");  // TODO
	}

	@Override
	public Function _concat() {
		return _concat;
	}

	@Override
	public Function _insert() {
		return _insert;
	}

	@Override
	public Function _move() {
		return _move;
	}

	@Override
	public Function _pack() {
		return Pack.INSTANCE;
	}

	@Override
	public Function _remove() {
		return _remove;
	}

	@Override
	public Function _sort() {
		return _sort;
	}

	@Override
	public Function _unpack() {
		return Unpack.INSTANCE;
	}


	public static class Pack extends AbstractLibFunction {

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

			context.getReturnBuffer().setTo(table);
		}

	}

	public static class Unpack extends AbstractLibFunction {

		public static final Unpack INSTANCE = new Unpack();

		@Override
		protected String name() {
			return "unpack";
		}

		private static class SuspendedState {

			public final int state;

			public final Object obj;
			public final int i;
			public final int j;
			public final int k;
			public final ArrayList<Object> result;

			public SuspendedState(int state, Object obj, int i, int j, int k, ArrayList<Object> result) {
				this.state = state;
				this.obj = obj;
				this.i = i;
				this.j = j;
				this.k = k;
				this.result = result;
			}

		}

		private static boolean hasLenMetamethod(ExecutionContext context, Table t) {
			return Metatables.getMetamethod(context.getState(), Metatables.MT_LEN, t) != null;
		}

		private static boolean hasIndexMetamethod(ExecutionContext context, Table t) {
			return Metatables.getMetamethod(context.getState(), Metatables.MT_INDEX, t) != null;
		}

		private static void unpackUsingRawGet(ExecutionContext context, Table t, int i, int j) {
			ArrayList<Object> r = new ArrayList<>();
			for (int k = i; k <= j; k++) {
				r.add(t.rawget(k));
			}
			context.getReturnBuffer().setToArray(r.toArray(new Object[r.size()]));
		}

		private static final int STATE_LEN_PREPARE = 0;
		private static final int STATE_LEN_RESUME = 1;
		private static final int STATE_BEFORE_LOOP = 2;
		private static final int STATE_LOOP = 3;

		private void run(ExecutionContext context, int state, Object obj, int i, int j, int k, ArrayList<Object> result) throws ControlThrowable {
			try {
				switch (state) {

					case STATE_LEN_PREPARE:
						state = STATE_LEN_RESUME;
						Dispatch.len(context, obj);  // may suspend, will pass #obj through the stack

					case STATE_LEN_RESUME: {
						Object o = context.getReturnBuffer()._0();
						Long l = Conversions.integerValueOf(o);
						if (l == null) {
							throw new LuaRuntimeException("object length is not an integer");
						}
						j = (int) l.longValue();
					}

					case STATE_BEFORE_LOOP:
						// j is known;

						// is this a clean table without __index?
						if (obj instanceof Table && !hasIndexMetamethod(context, (Table) obj)) {
							unpackUsingRawGet(context, (Table) obj, i, j);
							return;
						}

						// generic case: go through Dispatch and be prepared to be suspended;
						// k is the index running from i to j (inclusive)

						if (i <= j) {
							k = i;
							state = STATE_LOOP;
							result = new ArrayList<>();  // allocate the result accumulator
							Dispatch.index(context, obj, k++);  // may suspend

							// fall-through to state == STATE_LOOP
						}
						else {
							// interval empty, we're done
							context.getReturnBuffer().setTo();
							return;
						}

					case STATE_LOOP: {
						while (true) {
							// k now points to the *next* item to retrieve; we've just processed
							// (k - 1), need to add it to the results

							Object v = context.getReturnBuffer()._0();
							result.add(v);

							// we may now continue
							if (k <= j) {
								state = STATE_LOOP;
								Dispatch.index(context, obj, k++);  // may suspend
							}
							else {
								break;
							}
						}

						assert (k > j);

						// we're done!
						context.getReturnBuffer().setToArray(result.toArray(new Object[result.size()]));
						return;
					}

					default:
						throw new IllegalStateException("Illegal state: " + state);
				}
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, new SuspendedState(state, obj, i, j, k, result));
			}
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Object obj = args.peekOrNil();
			args.skip();
			int i = args.optNextInt(1);

			final int state;
			final int j;

			if (args.hasNext() && args.peek() != null) {
				j = args.nextInt();
				state = STATE_BEFORE_LOOP;
			}
			else if (obj instanceof Table && !hasLenMetamethod(context, (Table) obj)) {
				j = ((Table) obj).rawlen();  // safe to use rawlen
				state = STATE_BEFORE_LOOP;
			}
			else {
				j = 0;  // placeholder, will be retrieved in the run() method
				state = STATE_LEN_PREPARE;
			}

			run(context, state, obj, i, j, 0, null);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			SuspendedState ss = (SuspendedState) suspendedState;
			run(context, ss.state, ss.obj, ss.i, ss.j, ss.k, ss.result);
		}

	}


}
