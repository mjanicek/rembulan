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

import net.sandius.rembulan.core.*;
import net.sandius.rembulan.core.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.TableLib;

import java.util.ArrayList;

public class DefaultTableLib extends TableLib {

	private final Function _move;
	private final Function _remove;
	private final Function _sort;

	public DefaultTableLib() {
		this._move = new UnimplementedFunction("table.move");  // TODO
		this._remove = new UnimplementedFunction("table.remove");  // TODO
		this._sort = new UnimplementedFunction("table.sort");  // TODO
	}

	@Override
	public Function _concat() {
		return Concat.INSTANCE;
	}

	@Override
	public Function _insert() {
		return Insert.INSTANCE;
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

	static boolean hasLenMetamethod(ExecutionContext context, Table t) {
		return Metatables.getMetamethod(context.getState(), Metatables.MT_LEN, t) != null;
	}

	static boolean hasIndexMetamethod(ExecutionContext context, Table t) {
		return Metatables.getMetamethod(context.getState(), Metatables.MT_INDEX, t) != null;
	}

	static boolean hasNewIndexMetamethod(ExecutionContext context, Table t) {
		return Metatables.getMetamethod(context.getState(), Metatables.MT_NEWINDEX, t) != null;
	}

	static int getLength(ReturnBuffer rbuf) {
		Object o = rbuf.get0();
		Long l = Conversions.integerValueOf(o);
		if (l == null) {
			throw new LuaRuntimeException("object length is not an integer");
		}
		long ll = l.longValue();

		// does it fit into a 32-bit int?
		int i = (int) ll;
		if ((long) i != ll) {
			throw new LuaRuntimeException("object length is not a 32-bit integer");
		}
		else {
			return i;
		}
	}

	public static class Concat extends AbstractLibFunction {

		public static final Concat INSTANCE = new Concat();

		@Override
		protected String name() {
			return "concat";
		}

		private static class SuspendedState {

			public final int state;

			public final Table t;
			public final String sep;
			public final int i;
			public final int j;
			public final int k;
			public final StringBuilder bld;

			public SuspendedState(int state, Table t, String sep, int i, int j, int k, StringBuilder bld) {
				this.state = state;
				this.t = t;
				this.sep = sep;
				this.i = i;
				this.j = j;
				this.k = k;
				this.bld = bld;
			}

		}

		private static void appendToBuilder(StringBuilder bld, int index, Object o) {
			String s = Conversions.stringValueOf(o);
			if (s != null) {
				bld.append(s);
			}
			else {
				throw new LuaRuntimeException("invalid value ("
						+ PlainValueTypeNamer.INSTANCE.typeNameOf(o)+ ") at index " + index
						+ " in table for 'concat'");
			}

		}

		private static void concatUsingRawGet(ExecutionContext context, Table t, String sep, int i, int j) {
			StringBuilder bld = new StringBuilder();
			for (int k = i; k <= j; k++) {
				Object o = t.rawget(k);
				appendToBuilder(bld, k, o);
				if (k + 1 <= j) {
					bld.append(sep);
				}
			}
			context.getReturnBuffer().setTo(bld.toString());
		}

		private static final int STATE_LEN_PREPARE = 0;
		private static final int STATE_LEN_RESUME = 1;
		private static final int STATE_BEFORE_LOOP = 2;
		private static final int STATE_LOOP = 3;

		private void run(ExecutionContext context, int state, Table t, String sep, int i, int j, int k, StringBuilder bld) throws ControlThrowable {
			try {
				switch (state) {
					case STATE_LEN_PREPARE:
						state = STATE_LEN_RESUME;
						Dispatch.len(context, t);  // may suspend, will pass #obj through the stack

					case STATE_LEN_RESUME: {
						j = getLength(context.getReturnBuffer());
					}

					case STATE_BEFORE_LOOP:
						// j is known;

						// is this a clean table without __index?
						if (!hasIndexMetamethod(context, t)) {
							concatUsingRawGet(context, t, sep, i, j);
							return;
						}

						// generic case: go through Dispatch and be prepared to be suspended;
						// k is the index running from i to j (inclusive)

						if (i <= j) {
							k = i;
							state = STATE_LOOP;
							bld = new StringBuilder();  // allocate the result accumulator
							Dispatch.index(context, t, k++);  // may suspend

							// fall-through to state == STATE_LOOP
						}
						else {
							// interval empty, we're done
							context.getReturnBuffer().setTo("");  // return the empty string
							return;
						}

					case STATE_LOOP: {
						while (true) {
							// k now points to the *next* item to retrieve; we've just processed
							// (k - 1), need to add it to the results

							Object v = context.getReturnBuffer().get0();
							appendToBuilder(bld, k, v);

							// we may now continue
							if (k <= j) {
								// append the separator
								bld.append(sep);

								state = STATE_LOOP;
								Dispatch.index(context, t, k++);  // may suspend
							}
							else {
								break;
							}
						}

						assert (k > j);

						// we're done!
						context.getReturnBuffer().setTo(bld.toString());
						return;
					}

					default:
						throw new IllegalStateException("Illegal state: " + state);
				}
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, new SuspendedState(state, t, sep, i, j, k, bld));
			}
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			final Table t = args.nextTable();

			final String sep = args.hasNext() && args.peek() != null ? args.nextString() : "";

			final int i = args.optNextInt(1);

			final int state;
			final int j;

			if (args.hasNext() && args.peek() != null) {
				j = args.nextInt();
				state = STATE_BEFORE_LOOP;
			}
			else if (!hasLenMetamethod(context, t)) {
				j = t.rawlen();  // safe to use rawlen
				state = STATE_BEFORE_LOOP;
			}
			else {
				j = 0;  // placeholder, will be retrieved in the run() method
				state = STATE_LEN_PREPARE;
			}

			run(context, state, t, sep, i, j, 0, null);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			SuspendedState ss = (SuspendedState) suspendedState;
			run(context, ss.state, ss.t, ss.sep, ss.i, ss.j, ss.k, ss.bld);
		}

	}

	public static class Insert extends AbstractLibFunction {

		public static final Insert INSTANCE = new Insert();

		@Override
		protected String name() {
			return "insert";
		}

		private static class SuspendedState {

			public final int state;
			public final Table t;
			public final Integer pos;
			public final int len;
			public final Object value;

			private SuspendedState(int state, Table t, Integer pos, int len, Object value) {
				this.state = state;
				this.t = t;
				this.pos = pos;
				this.len = len;
				this.value = value;
			}

		}

		private static final int PHASE_SHIFT = 3;
		private static final int _LEN  = 0;
		private static final int _LOOP = 1;
		private static final int _END  = 2;

		private static final int _LEN_PREPARE = (_LEN << PHASE_SHIFT) | 0;
		private static final int _LEN_RESUME  = (_LEN << PHASE_SHIFT) | 1;

		private static final int _LOOP_TEST   = (_LOOP << PHASE_SHIFT) | 0;
		private static final int _LOOP_TABGET = (_LOOP << PHASE_SHIFT) | 1;
		private static final int _LOOP_TABSET = (_LOOP << PHASE_SHIFT) | 2;

		private static final int _END_TABSET = (_END << PHASE_SHIFT) | 0;
		private static final int _END_RETURN = (_END << PHASE_SHIFT) | 1;

		private static void rawInsert(Table t, int pos, int len, Object value) {
			for (int k = len + 1; k > pos; k--) {
				t.rawset(k, t.rawget(k - 1));
			}
			t.rawset(pos, value);
		}

		private void checkValidPos(int pos, int len) {
			if (pos < 1 || pos > len + 1) {
				throw new BadArgumentException(2, name(), "position out of bounds");
			}
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Table t = args.nextTable();
			final int pos;
			final Object value;

			if (args.size() == 2) {
				// implicit pos (#t + 1)
				value = args.nextAny();

				if (!hasLenMetamethod(context, t)) {
					int len = t.rawlen();
					start_loop(context, t, len + 1, len, value);
				}
				else {
					_len(context, _LEN_PREPARE, t, null, value);
				}
			}
			else if (args.size() == 3) {
				// explicit pos
				pos = args.nextInt();
				value = args.nextAny();

				if (!hasLenMetamethod(context, t)) {
					int len = t.rawlen();
					checkValidPos(pos, len);
					start_loop(context, t, pos, len, value);
				}
				else {
					_len(context, _LEN_PREPARE, t, Integer.valueOf(pos), value);
				}
			}
			else {
				throw new LuaRuntimeException("wrong number of arguments to 'insert'");
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			SuspendedState ss = (SuspendedState) suspendedState;
			switch (ss.state >> PHASE_SHIFT) {
				case _LEN:  _len(context, ss.state, ss.t, ss.pos, ss.value); break;
				case _LOOP: _loop(context, ss.state, ss.t, ss.pos, ss.len, ss.value); break;
				case _END:  _end(context, ss.state, ss.t, ss.pos, ss.value); break;
				default: throw new IllegalStateException("Illegal state: " + ss.state);
			}
		}

		private void _len(ExecutionContext context, int state, Table t, Integer pos, Object value)
				throws ControlThrowable {

			// __len is defined, must go through Dispatch

			final int p;
			final int len;

			try {
				switch (state) {
					case _LEN_PREPARE:
						state = _LEN_RESUME;
						Dispatch.len(context, t);

					case _LEN_RESUME: {
						len = getLength(context.getReturnBuffer());
						if (pos != null) {
							// explicit pos
							p = pos.intValue();
							checkValidPos(p, len);
						}
						else {
							// implicit pos
							p = len + 1;
						}
						break;
					}

					default:
						throw new IllegalStateException("Illegal state: " + state);
				}
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, new SuspendedState(state, t, pos, 0, value));
			}

			// continue with the loop
			start_loop(context, t, p, len, value);
		}

		private void start_loop(ExecutionContext context, Table t, int pos, int len, Object value)
				throws ControlThrowable {

			// check whether we can use raw accesses instead of having to go through
			// Dispatch and potential metamethods

			if (!hasIndexMetamethod(context, t) && !hasNewIndexMetamethod(context, t)) {
				// raw case
				rawInsert(t, pos, len, value);
				context.getReturnBuffer().setTo();
			}
			else {
				// generic (Dispatch'd) case
				// initialise k = len + 1 (will be decremented in the next TEST, so add +1 here)
				_loop(context, _LOOP_TEST, t, pos, len + 2, value);
			}
		}

		private void _loop(ExecutionContext context, int state, Table t, int pos, int k, Object value)
				throws ControlThrowable {

			// came from start_loop in the invoke path

			try {
				loop: while (true) {
					switch (state) {
						case _LOOP_TEST:
							k -= 1;
							state = _LOOP_TABGET;
							if (k <= pos) {
								break loop;  // end the loop
							}

						case _LOOP_TABGET:
							state = _LOOP_TABSET;
							Dispatch.index(context, t, Long.valueOf(k - 1));

						case _LOOP_TABSET:
							state = _LOOP_TEST;
							Object v = context.getReturnBuffer().get0();
							Dispatch.setindex(context, t, Long.valueOf(k), v);
							break;  // go to next iteration

						default:
							throw new IllegalStateException("Illegal state: " + state);

					}
				}
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, new SuspendedState(state, t, Integer.valueOf(pos), k, value));
			}

			// continue into the last stage
			state = _END_TABSET;
			_end(context, state, t, pos, value);
		}

		private void _end(ExecutionContext context, int state, Table t, int pos, Object value)
				throws ControlThrowable {
			try {
				switch (state) {

					case _END_TABSET:
						state = _END_RETURN;
						Dispatch.setindex(context, t, Long.valueOf(pos), value);

					case _END_RETURN:
						break;

					default:
						throw new IllegalStateException("Illegal state: " + state);
				}
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, new SuspendedState(state, t, Integer.valueOf(pos), -1, value));
			}

			// finished!
			context.getReturnBuffer().setTo();
		}

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

		private static void unpackUsingRawGet(ExecutionContext context, Table t, int i, int j) {
			ArrayList<Object> r = new ArrayList<>();
			for (int k = i; k <= j; k++) {
				r.add(t.rawget(k));
			}
			context.getReturnBuffer().setToContentsOf(r);
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
						j = getLength(context.getReturnBuffer());
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

							Object v = context.getReturnBuffer().get0();
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
						context.getReturnBuffer().setToContentsOf(result);
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
