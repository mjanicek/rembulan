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

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.Function;
import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.Metatables;
import net.sandius.rembulan.PlainValueTypeNamer;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.TableLib;
import net.sandius.rembulan.runtime.Dispatch;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.ReturnBuffer;

import java.util.ArrayList;

public class DefaultTableLib extends TableLib {

	private final Function _move;
	private final Function _sort;

	public DefaultTableLib() {
		this._move = new UnimplementedFunction("table.move");  // TODO
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
		return Remove.INSTANCE;
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
			public final ArgumentIterator args;
			public final String sep;
			public final int i;
			public final int j;
			public final int k;
			public final StringBuilder bld;

			public SuspendedState(int state, Table t, ArgumentIterator args, String sep, int i, int j, int k, StringBuilder bld) {
				this.state = state;
				this.t = t;
				this.args = args;
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

		private void run(ExecutionContext context, int state, Table t, ArgumentIterator args, String sep, int i, int j, int k, StringBuilder bld)
				throws ControlThrowable {

			try {
				switch (state) {
					// entry point for t with __len
					case STATE_LEN_PREPARE:
						state = STATE_LEN_RESUME;
						Dispatch.len(context, t);  // may suspend, will pass #obj through the stack

					// resume point #1
					case STATE_LEN_RESUME: {
						// pass length in k
						k = getLength(context.getReturnBuffer());
					}

					// entry point for t without __len; k == #t
					case STATE_BEFORE_LOOP: {
						int len = k;
						k = 0;  // clear k

						// process arguments
						sep = args.hasNext() && args.peek() != null ? args.nextString() : "";
						i = args.optNextInt(1);
						j = args.hasNext() && args.peek() != null ? args.nextInt() : len;

						// don't need the args any more
						args = null;

						// i, j are known, k is 0

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
				throw ct.push(this, new SuspendedState(state, t, args, sep, i, j, k, bld));
			}
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			final Table t = args.nextTable();
			final int state;
			final int k;

			if (!hasLenMetamethod(context, t)) {
				// safe to use rawlen
				state = STATE_BEFORE_LOOP;
				k = t.rawlen();
			}
			else {
				// must use Dispatch
				state = STATE_LEN_PREPARE;
				k = 0;  // dummy value, will be overwritten
			}

			run(context, state, t, args, null, 0, 0, k, null);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			SuspendedState ss = (SuspendedState) suspendedState;
			run(context, ss.state, ss.t, ss.args, ss.sep, ss.i, ss.j, ss.k, ss.bld);
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
			public final ArgumentIterator args;
			public final int pos;
			public final int len;
			public final Object value;

			private SuspendedState(int state, Table t, ArgumentIterator args, int pos, int len, Object value) {
				this.state = state;
				this.t = t;
				this.args = args;
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

		private void checkValidPos(int pos, int len) {
			if (pos < 1 || pos > len + 1) {
				throw new BadArgumentException(2, name(), "position out of bounds");
			}
		}

		private static void rawInsert(Table t, int pos, int len, Object value) {
			for (int k = len + 1; k > pos; k--) {
				t.rawset(k, t.rawget(k - 1));
			}
			t.rawset(pos, value);
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Table t = args.nextTable();

			if (!hasLenMetamethod(context, t)) {
				run_args(context, t, args, t.rawlen());
			}
			else {
				_len(context, _LEN_PREPARE, t, args);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			SuspendedState ss = (SuspendedState) suspendedState;
			switch (ss.state >> PHASE_SHIFT) {
				case _LEN:  _len(context, ss.state, ss.t, ss.args); break;
				case _LOOP: _loop(context, ss.state, ss.t, ss.pos, ss.len, ss.value); break;
				case _END:  _end(context, ss.state, ss.t, ss.pos, ss.value); break;
				default: throw new IllegalStateException("Illegal state: " + ss.state);
			}
		}

		private void _len(ExecutionContext context, int state, Table t, ArgumentIterator args)
				throws ControlThrowable {

			// __len is defined, must go through Dispatch

			final int len;

			try {
				switch (state) {
					case _LEN_PREPARE:
						state = _LEN_RESUME;
						Dispatch.len(context, t);

					case _LEN_RESUME: {
						len = getLength(context.getReturnBuffer());
						break;
					}

					default:
						throw new IllegalStateException("Illegal state: " + state);
				}
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, new SuspendedState(state, t, args, 0, 0, null));
			}

			// next: process the arguments
			run_args(context, t, args, len);
		}

		private void run_args(ExecutionContext context, Table t, ArgumentIterator args, int len)
				throws ControlThrowable {

			final int pos;
			final Object value;

			if (args.size() == 2) {
				// implicit pos (#t + 1)
				pos = len + 1;
				value = args.nextAny();
			}
			else if (args.size() == 3) {
				// explicit pos
				pos = args.nextInt();
				checkValidPos(pos, len);
				value = args.nextAny();
			}
			else {
				throw new LuaRuntimeException("wrong number of arguments to 'insert'");
			}

			// next: start the loop
			start_loop(context, t, pos, len, value);
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
				throw ct.push(this, new SuspendedState(state, t, null, pos, k, value));
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
				throw ct.push(this, new SuspendedState(state, t, null, pos, -1, value));
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

	public static class Remove extends AbstractLibFunction {

		public static final Remove INSTANCE = new Remove();

		@Override
		protected String name() {
			return "remove";
		}

		private static class SuspendedState {

			public final int state;
			public final Table t;
			public final ArgumentIterator args;
			public final int pos;
			public final int len;
			public final Object result;

			private SuspendedState(int state, Table t, ArgumentIterator args, int pos, int len, Object result) {
				this.state = state;
				this.t = t;
				this.args = args;
				this.pos = pos;
				this.len = len;
				this.result = result;
			}

		}

		private static final int PHASE_SHIFT = 4;
		private static final int _LEN  = 0;
		private static final int _GET  = 1;
		private static final int _LOOP = 2;
		private static final int _ERASE  = 4;

		private static final int _LEN_PREPARE = (_LEN << PHASE_SHIFT) | 0;
		private static final int _LEN_RESUME  = (_LEN << PHASE_SHIFT) | 1;

		private static final int _GET_PREPARE = (_GET << PHASE_SHIFT) | 0;
		private static final int _GET_RESUME  = (_GET << PHASE_SHIFT) | 1;

		private static final int _LOOP_TEST   = (_LOOP << PHASE_SHIFT) | 0;
		private static final int _LOOP_TABGET = (_LOOP << PHASE_SHIFT) | 1;
		private static final int _LOOP_TABSET = (_LOOP << PHASE_SHIFT) | 2;

		private static final int _ERASE_PREPARE = (_ERASE << PHASE_SHIFT) | 0;
		private static final int _ERASE_RESUME  = (_ERASE << PHASE_SHIFT) | 1;

		private void checkValidPos(int pos, int len) {
			if (!((len == 0 && pos == 0) || pos == len)  // the no-shift case
					&& (pos < 1 || pos > len + 1)) {
				throw new BadArgumentException(2, name(), "position out of bounds");
			}
		}

		private static Object rawRemove(Table t, int pos, int len) {
			Object result = t.rawget(pos);

			if (pos == 0 || pos == len || pos == len + 1) {
				// erase t[pos]
				t.rawset(pos, null);
			}
			else {
				// shift down t[pos+1],...,t[len]; erase t[len]
				for (int k = pos + 1; k <= len; k++) {
					t.rawset(k - 1, t.rawget(k));
				}
				t.rawset(len, null);
			}

			return result;
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Table t = args.nextTable();

			if (!hasLenMetamethod(context, t)) {
				run_args(context, t, args, t.rawlen());
			}
			else {
				_len(context, _LEN_PREPARE, t, args);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			SuspendedState ss = (SuspendedState) suspendedState;
			switch (ss.state >> PHASE_SHIFT) {
				case _LEN:   _len(context, ss.state, ss.t, ss.args); break;
				case _GET:   _get_result(context, ss.state, ss.t, ss.pos, ss.len); break;
				case _LOOP:  _loop(context, ss.state, ss.t, ss.pos, ss.len, ss.result); break;
				case _ERASE: _erase(context, ss.state, ss.t, 0, ss.result); break;  // index not needed any more when resuming
				default: throw new IllegalStateException("Illegal state: " + ss.state);
			}
		}

		private void _len(ExecutionContext context, int state, Table t, ArgumentIterator args)
				throws ControlThrowable {

			// __len is defined, must go through Dispatch

			final int len;

			try {
				switch (state) {
					case _LEN_PREPARE:
						state = _LEN_RESUME;
						Dispatch.len(context, t);

					case _LEN_RESUME: {
						len = getLength(context.getReturnBuffer());
						break;
					}

					default:
						throw new IllegalStateException("Illegal state: " + state);
				}
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, new SuspendedState(state, t, args, 0, 0, null));
			}

			// next: process the arguments
			run_args(context, t, args, len);
		}

		private void run_args(ExecutionContext context, Table t, ArgumentIterator args, int len)
				throws ControlThrowable {

			final int pos;

			if (args.hasNext() && args.peek() != null) {
				// explicit pos
				pos = args.nextInt();
				checkValidPos(pos, len);
			}
			else {
				// implicit pos (#t)
				pos = len;
			}

			// next: start the loop
			start_loop(context, t, pos, len);
		}

		private void start_loop(ExecutionContext context, Table t, int pos, int len)
				throws ControlThrowable {

			// check whether we can use raw accesses instead of having to go through
			// Dispatch and potential metamethods

			if (!hasIndexMetamethod(context, t) && !hasNewIndexMetamethod(context, t)) {
				// raw case
				Object result = rawRemove(t, pos, len);
				context.getReturnBuffer().setTo(result);
			}
			else {
				// generic (Dispatch'd) case
				// initialise k = len + 1 (will be decremented in the next TEST, so add +1 here)
				_get_result(context, _GET_PREPARE, t, pos, len);
			}
		}

		private void _get_result(ExecutionContext context, int state, Table t, int pos, int len)
				throws ControlThrowable {

			final Object result;
			try {
				switch (state) {
					case _GET_PREPARE:
						state = _GET_RESUME;
						Dispatch.index(context, t, pos);

					case _GET_RESUME:
						result = context.getReturnBuffer().get0();
					    break;

					default:
						throw new IllegalStateException("Illegal state: " + state);
				}
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, new SuspendedState(state, t, null, pos, len, null));
			}


			if (pos == 0 || pos == len || pos == len + 1) {
				// erase t[pos]
				_erase(context, _ERASE_PREPARE, t, pos, result);
			}
			else {
				// pos now used for iteration; want (pos + 1), but it will be incremented before the test
				_loop(context, _LOOP_TEST, t, pos, len, result);
			}
		}

		private void _loop(ExecutionContext context, int state, Table t, int k, int len, Object result)
				throws ControlThrowable {

			// came from start_loop in the invoke path

			try {
				loop: while (true) {
					switch (state) {
						case _LOOP_TEST:
							k += 1;
							state = _LOOP_TABGET;
							if (k > len) {
								break loop;  // end the loop
							}

						case _LOOP_TABGET:
							state = _LOOP_TABSET;
							Dispatch.index(context, t, Long.valueOf(k));

						case _LOOP_TABSET:
							state = _LOOP_TEST;
							Object v = context.getReturnBuffer().get0();
							Dispatch.setindex(context, t, Long.valueOf(k - 1), v);
							break;  // go to next iteration

						default:
							throw new IllegalStateException("Illegal state: " + state);

					}
				}
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, new SuspendedState(state, t, null, k, len, result));
			}

			// erase the last element, return the result
			_erase(context, _ERASE_PREPARE, t, len, result);
		}

		private void _erase(ExecutionContext context, int state, Table t, int idx, Object result)
				throws ControlThrowable {

			try {
				switch (state) {
					case _ERASE_PREPARE:
						state = _ERASE_RESUME;
						Dispatch.setindex(context, t, Long.valueOf(idx), null);

					case _ERASE_RESUME:
						// we're done
						break;

					default:
						throw new IllegalStateException("Illegal state: " + state);
				}
			}
			catch (ControlThrowable ct) {
				// no need to carry any information but the result around
				throw ct.push(this, new SuspendedState(state, null, null, 0, 0, result));
			}

			// finished, set the result
			context.getReturnBuffer().setTo(result);
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
