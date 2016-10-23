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

package net.sandius.rembulan.lib;

import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Userdata;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * This library provides the functionality of the debug interface (see §4.9 of the Lua
 * Reference Manual) to Lua programs. You should exert care when using this library.
 * Several of its functions violate basic assumptions about Lua code (e.g., that variables
 * local to a function cannot be accessed from outside; that userdata metatables cannot
 * be changed by Lua code; that Lua programs do not crash) and therefore can compromise
 * otherwise secure code. Moreover, some functions in this library may be slow.
 *
 * <p>All functions in this library are provided inside the {@code debug} table. All functions
 * that operate over a thread have an optional first argument which is the thread to operate
 * over. The default is always the current thread.</p>
 */
public final class DebugLib {

	static final LuaFunction DEBUG = new Debug();
	static final LuaFunction GETHOOK = new GetHook();
	static final LuaFunction GETINFO = new GetInfo();
	static final LuaFunction GETLOCAL = new GetLocal();
	static final LuaFunction GETMETATABLE = new GetMetatable();
	static final LuaFunction GETREGISTRY = new GetRegistry();
	static final LuaFunction GETUPVALUE = new GetUpvalue();
	static final LuaFunction GETUSERVALUE = new GetUserValue();
	static final LuaFunction SETHOOK = new SetHook();
	static final LuaFunction SETLOCAL = new SetLocal();
	static final LuaFunction SETMETATABLE = new SetMetatable();
	static final LuaFunction SETUPVALUE = new SetUpvalue();
	static final LuaFunction SETUSERVALUE = new SetUserValue();
	static final LuaFunction TRACEBACK = new Traceback();
	static final LuaFunction UPVALUEID = new UpvalueId();
	static final LuaFunction UPVALUEJOIN = new UpvalueJoin();
	
	/**
	 * Returns the function {@code debug.debug}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.debug ()}
	 *
	 * <p>Enters an interactive mode with the user, running each string that the user enters.
	 * Using simple commands and other debug facilities, the user can inspect global and local
	 * variables, change their values, evaluate expressions, and so on. A line containing only
	 * the word {@code cont} finishes this function, so that the caller continues
	 * its execution.</p>
	 *
	 * <p>Note that commands for {@code debug.debug} are not lexically nested within any
	 * function and so have no direct access to local variables.</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.debug} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.debug">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.debug</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction debug() {
		return DEBUG;
	}

	/**
	 * Returns the function {@code debug.gethook}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.gethook ([thread])}
	 *
	 * <p>Returns the current hook settings of the thread, as three values: the current hook
	 * function, the current hook mask, and the current hook count (as set by
	 * the {@link #sethook() <code>debug.sethook</code>} function).</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.gethook} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.gethook">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.gethook</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction gethook() {
		return GETHOOK;
	}

	/**
	 * Returns the function {@code debug.getinfo}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.getinfo ([thread,] f [, what])}
	 *
	 * <p>Returns a table with information about a function. You can give the function directly
	 * or you can give a number as the value of {@code f}, which means the function running
	 * at level {@code f} of the call stack of the given thread: level 0 is the current
	 * function ({@code getinfo} itself); level 1 is the function that called {@code getinfo}
	 * (except for tail calls, which do not count on the stack); and so on. If {@code f}
	 * is a number larger than the number of active functions, then {@code getinfo}
	 * returns <b>nil</b>.</p>
	 *
	 * <p>The returned table can contain all the fields returned by {@code lua_getinfo},
	 * with the string {@code what} describing which fields to fill in. The default for
	 * {@code what} is to get all information available, except the table of valid lines.
	 * If present, the option {@code 'f'} adds a field named {@code func} with the function
	 * itself. If present, the option {@code 'L'} adds a field named {@code activelines}
	 * with the table of valid lines.</p>
	 *
	 * <p>For instance, the expression {@code debug.getinfo(1,"n").name} returns a name for
	 * the current function, if a reasonable name can be found, and the expression
	 * {@code debug.getinfo(print)} returns a table with all available information about
	 * the {@code print} function.</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.getinfo} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.getinfo">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.getinfo</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction getinfo() {
		return GETINFO;
	}

	/**
	 * Returns the function {@code debug.getlocal}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.getlocal ([thread,] f, local)}
	 *
	 * <p>This function returns the name and the value of the local variable with index local
	 * of the function at level {@code f} of the stack. This function accesses not only explicit
	 * local variables, but also parameters, temporaries, etc.</p>
	 *
	 * <p>The first parameter or local variable has index 1, and so on, following the order
	 * that they are declared in the code, counting only the variables that are active in
	 * the current scope of the function. Negative indices refer to vararg parameters;
	 * -1 is the first vararg parameter. The function returns <b>nil</b> if there is no variable
	 * with the given index, and raises an error when called with a level out of range.
	 * (You can call {@link #getinfo() <code>debug.getinfo</code>} to check whether the level
	 * is valid.)</p>
	 *
	 * <p>Variable names starting with {@code '('} (open parenthesis) represent variables with
	 * no known names (internal variables such as loop control variables, and variables from
	 * chunks saved without debug information).</p>
	 *
	 * <p>The parameter {@code f} may also be a function. In that case, {@code getlocal}
	 * returns only the name of function parameters.</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.getlocal} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.getlocal">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.getlocal</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction getlocal() {
		return GETLOCAL;
	}

	/**
	 * Returns the function {@code debug.getmetatable}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.getmetatable (value)}
	 *
	 * <p>Returns the metatable of the given {@code value} or <b>nil</b> if it does not have
	 * a metatable.</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.getmetatable} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.getmetatable">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.getmetatable</code></a>
	 */
	public static LuaFunction getmetatable() {
		return GETMETATABLE;
	}

	/**
	 * Returns the function {@code debug.getregistry}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.getregistry ()}
	 *
	 * <p>Returns the registry table (see §4.5 of the Lua Reference Manual).</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.getregistry} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.getregistry">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.getregistry</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction getregistry() {
		return GETREGISTRY;
	}

	/**
	 * Returns the function {@code debug.getupvalue}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.getupvalue (f, up)}
	 *
	 * <p>This function returns the name and the value of the upvalue with index {@code up}
	 * of the function {@code f}. The function returns <b>nil</b> if there is no upvalue with
	 * the given index.</p>
	 *
	 * <p>Variable names starting with {@code '('} (open parenthesis) represent variables with
	 * no known names (variables from chunks saved without debug information).</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.getupvalue} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.getupvalue">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.getupvalue</code></a>
	 */
	public static LuaFunction getupvalue() {
		return GETUPVALUE;
	}

	/**
	 * Returns the function {@code debug.getuservalue}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.getuservalue (u)}
	 *
	 * <p>Returns the Lua value associated to {@code u}. If {@code u} is not a userdata,
	 * returns <b>nil</b>.</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.getuservalue} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.getuservalue">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.getuservalue</code></a>
	 */
	public static LuaFunction getuservalue() {
		return GETUSERVALUE;
	}

	/**
	 * Returns the function {@code debug.sethook}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.sethook ([thread,] hook, mask [, count])}
	 *
	 * <p>Sets the given function as a hook. The string {@code mask} and the number {@code count}
	 * describe when the hook will be called. The string {@code mask} may have any combination
	 * of the following characters, with the given meaning:</p>
	 *
	 * <ul>
	 * <li><b>{@code 'c'}</b>: the hook is called every time Lua calls a function;</li>
	 * <li><b>{@code 'r'}</b>: the hook is called every time Lua returns from a function;</li>
	 * <li><b>{@code 'l'}</b>: the hook is called every time Lua enters a new line of code.</li>
	 * </ul>
	 *
	 * <p>Moreover, with a {@code count} different from zero, the hook is called also after
	 * every {@code count} instructions.</p>
	 *
	 * <p>When called without arguments, {@code debug.sethook} turns off the hook.</p>
	 *
	 * <p>When the hook is called, its first parameter is a string describing the event that
	 * has triggered its call: {@code "call"} (or {@code "tail call"}), {@code "return"},
	 * {@code "line"}, and {@code "count"}. For line events, the hook also gets the new line
	 * number as its second parameter. Inside a hook, you can call {@code getinfo} with level 2
	 * to get more information about the running function (level 0 is the {@code getinfo}
	 * function, and level 1 is the hook function).</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.sethook} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.sethook">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.sethook</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction sethook() {
		return SETHOOK;
	}

	/**
	 * Returns the function {@code debug.setlocal}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.setlocal ([thread,] level, local, value)}
	 *
	 * <p>This function assigns the value {@code value} to the local variable with index
	 * {@code local} of the function at {@code level} level of the stack. The function returns
	 * <b>nil</b> if there is no local variable with the given index, and raises an error when
	 * called with a level out of range. (You can call {@link #getinfo() <code>getinfo</code>}
	 * to check whether the level is valid.) Otherwise, it returns the name of the local
	 * variable.</p>
	 *
	 * <p>See {@link #getlocal() <code>debug.getlocal</code>} for more information about variable
	 * indices and names.</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.setlocal} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.setlocal">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.setlocal</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction setlocal() {
		return SETLOCAL;
	}

	/**
	 * Returns the function {@code debug.setmetatable}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.setmetatable (value, table)}
	 *
	 * <p>Sets the metatable for the given {@code value} to the given {@code table} (which can
	 * be <b>nil</b>). Returns {@code value}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.setmetatable} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.setmetatable">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.setmetatable</code></a>
	 */
	public static LuaFunction setmetatable() {
		return SETMETATABLE;
	}

	/**
	 * Returns the function {@code debug.setupvalue}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.setupvalue (f, up, value)}
	 *
	 * <p>This function assigns the value {@code value} to the upvalue with index {@code up}
	 * of the function f. The function returns <b>nil</b> if there is no upvalue with the given
	 * index. Otherwise, it returns the name of the upvalue.</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.setupvalue} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.setupvalue">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.setupvalue</code></a>
	 */
	public static LuaFunction setupvalue() {
		return SETUPVALUE;
	}

	/**
	 * Returns the function {@code debug.setuservalue}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.setuservalue (udata, value)}
	 *
	 * <p>Sets the given {@code value} as the Lua value associated to the given {@code udata}.
	 * udata must be a full userdata.</p>
	 *
	 * <p>Returns {@code udata}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.setuservalue} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.setuservalue">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.setuservalue</code></a>
	 */
	public static LuaFunction setuservalue() {
		return SETUSERVALUE;
	}

	/**
	 * Returns the function {@code debug.traceback}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.traceback ([thread,] [message [, level]])}
	 *
	 * <p>If {@code message} is present but is neither a string nor <b>nil</b>, this function
	 * returns message without further processing. Otherwise, it returns a string with
	 * a traceback of the call stack. The optional {@code message} string is appended at
	 * the beginning of the traceback. An optional {@code level} number tells at which level
	 * to start the traceback (default is 1, the function calling {@code traceback}).</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.traceback} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.traceback">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.traceback</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction traceback() {
		return TRACEBACK;
	}

	/**
	 * Returns the function {@code debug.upvalueid}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.upvalueid (f, n)}
	 *
	 * <p>Returns a unique identifier (as a light userdata) for the upvalue numbered {@code n}
	 * from the given function.</p>
	 *
	 * <p>These unique identifiers allow a program to check whether different closures share
	 * upvalues. Lua closures that share an upvalue (that is, that access a same external
	 * local variable) will return identical ids for those upvalue indices.</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.upvalueid} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.upvalueid">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.upvalueid</code></a>
	 */
	public static LuaFunction upvalueid() {
		return UPVALUEID;
	}

	/**
	 * Returns the function {@code debug.upvaluejoin}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code debug.upvaluejoin (f1, n1, f2, n2)}
	 *
	 * <p>Make the {@code n1}-th upvalue of the Lua closure {@code f1} refer to
	 * the {@code n2}-th upvalue of the Lua closure {@code f2}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code debug.upvaluejoin} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-debug.upvaluejoin">
	 *     the Lua 5.3 Reference Manual entry for <code>debug.upvaluejoin</code></a>
	 */
	public static LuaFunction upvaluejoin() {
		return UPVALUEJOIN;
	}

	
	private DebugLib() {
		// not to be instantiated
	}

	/**
	 * Installs the debug library to the global environment {@code env} in the state
	 * context {@code context}.
	 *
	 * @param context  the state context, must not be {@code null}
	 * @param env  the global environment, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code context} or {@code env} is {@code null}
	 */
	public static void installInto(StateContext context, Table env) {
		Objects.requireNonNull(context);
		Objects.requireNonNull(env);

		Table t = context.newTable();

		t.rawset("debug", debug());
		t.rawset("gethook", gethook());
		t.rawset("getinfo", getinfo());
		t.rawset("getlocal", getlocal());
		t.rawset("getmetatable", getmetatable());
		t.rawset("getregistry", getregistry());
		t.rawset("getupvalue", getupvalue());
		t.rawset("getuservalue", getuservalue());
		t.rawset("sethook", sethook());
		t.rawset("setlocal", setlocal());
		t.rawset("setmetatable", setmetatable());
		t.rawset("setupvalue", setupvalue());
		t.rawset("setuservalue", setuservalue());
		t.rawset("traceback", traceback());
		t.rawset("upvalueid", upvalueid());
		t.rawset("upvaluejoin", upvaluejoin());

		ModuleLib.install(env, "debug", t);
	}

	static class UpvalueRef {

		private final int index;
		private final LuaFunction function;
		private final Field field;

		public UpvalueRef(int index, LuaFunction function, Field field) {
			this.index = index;
			this.function = Objects.requireNonNull(function);
			this.field = Objects.requireNonNull(field);
		}

		// index is 0-based
		public static UpvalueRef find(LuaFunction f, int index) {
			Objects.requireNonNull(f);

			// find the index-th upvalue field
			int idx = 0;
			for (Field fld : f.getClass().getDeclaredFields()) {
				Class<?> fldType = fld.getType();
				if (Variable.class.isAssignableFrom(fldType)) {
					if (idx == index) {
						// found it
						fld.setAccessible(true);
						return new UpvalueRef(index, f, fld);
					}
					else {
						idx += 1;
					}
				}
			}

			return null;
		}

		public String name() {
			return field.getName();
		}

		public int index() {
			return index;
		}

		public Variable get() throws IllegalAccessException {
			return (Variable) field.get(function);
		}

		public void set(Variable ref) throws IllegalAccessException {
			Objects.requireNonNull(ref);
			field.set(function, ref);
		}

	}

	static class Debug extends UnimplementedFunction {
		// TODO
		public Debug() {
			super("debug.debug");
		}
	}

	static class GetInfo extends UnimplementedFunction {
		// TODO
		public GetInfo() {
			super("debug.getinfo");
		}
	}

	static class GetHook extends UnimplementedFunction {
		// TODO
		public GetHook() {
			super("debug.gethook");
		}
	}

	static class SetHook extends UnimplementedFunction {
		// TODO
		public SetHook() {
			super("debug.sethook");
		}
	}

	static class GetLocal extends UnimplementedFunction {
		// TODO
		public GetLocal() {
			super("debug.getlocal");
		}
	}

	static class SetLocal extends UnimplementedFunction {
		// TODO
		public SetLocal() {
			super("debug.setlocal");
		}
	}

	static class GetRegistry extends UnimplementedFunction {
		// TODO
		public GetRegistry() {
			super("debug.getregistry");
		}
	}

	static class GetMetatable extends AbstractLibFunction {

		@Override
		protected String name() {
			return "getmetatable";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object value = args.nextAny();
			Table mt = context.getMetatable(value);
			context.getReturnBuffer().setTo(mt);
		}

	}

	static class SetMetatable extends AbstractLibFunction {

		@Override
		protected String name() {
			return "setmetatable";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object value = args.peekOrNil();
			args.skip();
			Table mt = args.nextTableOrNil();

			// set the new metatable
			context.setMetatable(value, mt);

			// return value
			context.getReturnBuffer().setTo(value);
		}

	}

	static class GetUpvalue extends AbstractLibFunction {

		@Override
		protected String name() {
			return "getupvalue";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			args.skip();
			int index = args.nextInt();
			args.rewind();
			LuaFunction f = args.nextFunction();

			UpvalueRef uvRef = UpvalueRef.find(f, index - 1);

			if (uvRef != null) {
				final String name;
				final Object value;

				try {
					name = uvRef.name();
					Variable uv = uvRef.get();
					value = uv.get();
				}
				catch (IllegalAccessException ex) {
					throw new LuaRuntimeException(ex);
				}

				context.getReturnBuffer().setTo(name, value);
			}
			else {
				// contrary to what its documentation says, PUC-Lua 5.3.2 doesn't seem to return
				// nil but rather an empty list
				context.getReturnBuffer().setTo();
			}

		}

	}

	static class SetUpvalue extends AbstractLibFunction {

		@Override
		protected String name() {
			return "setupvalue";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			args.skip();
			args.skip();
			Object newValue = args.nextAny();
			args.rewind();
			args.skip();
			int index = args.nextInt();
			args.rewind();
			LuaFunction f = args.nextFunction();

			UpvalueRef uvRef = UpvalueRef.find(f, index - 1);

			final String name;

			if (uvRef != null) {
				try {
					name = uvRef.name();
					Variable uv = uvRef.get();
					uv.set(newValue);
				}
				catch (IllegalAccessException ex) {
					throw new LuaRuntimeException(ex);
				}
			}
			else {
				name = null;
			}

			context.getReturnBuffer().setTo(name);
		}

	}

	static class UpvalueId extends AbstractLibFunction {

		@Override
		protected String name() {
			return "upvalueid";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			args.goTo(1);
			int n = args.nextInt();
			args.goTo(0);
			LuaFunction f = args.nextFunction();

			UpvalueRef uvRef = UpvalueRef.find(f, n - 1);
			if (uvRef == null) {
				throw new BadArgumentException(2, name(), "invalid upvalue index");
			}
			else {
				final Variable uv;
				try {
					uv = uvRef.get();
				}
				catch (IllegalAccessException ex) {
					throw new LuaRuntimeException(ex);
				}

				context.getReturnBuffer().setTo(uv);
			}
		}

	}

	static class UpvalueJoin extends AbstractLibFunction {

		@Override
		protected String name() {
			return "upvaluejoin";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			// read f1, n1
			args.goTo(1);
			int n1 = args.nextInt();
			args.goTo(0);
			LuaFunction f1 = args.nextFunction();

			UpvalueRef uvRef1 = UpvalueRef.find(f1, n1 - 1);
			if (uvRef1 == null) {
				throw new BadArgumentException(2, name(), "invalid upvalue index");
			}

			// read f2, n2
			args.goTo(3);
			int n2 = args.nextInt();
			args.goTo(2);
			LuaFunction f2 = args.nextFunction();

			UpvalueRef uvRef2 = UpvalueRef.find(f2, n2 - 1);
			if (uvRef2 == null) {
				throw new BadArgumentException(4, name(), "invalid upvalue index");
			}

			try {
				uvRef1.set(uvRef2.get());
			}
			catch (IllegalAccessException ex) {
				throw new LuaRuntimeException(ex);
			}

			context.getReturnBuffer().setTo();
		}

	}

	static class GetUserValue extends AbstractLibFunction {

		@Override
		protected String name() {
			return "getuservalue";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object o = args.peekOrNil();

			Object result = o instanceof Userdata ? ((Userdata) o).getUserValue() : null;
			context.getReturnBuffer().setTo(result);
		}

	}

	static class SetUserValue extends AbstractLibFunction {

		@Override
		protected String name() {
			return "setuservalue";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Userdata userdata = args.nextUserdata();
			Object value = args.nextAny();

			userdata.setUserValue(value);
			context.getReturnBuffer().setTo(userdata);
		}

	}

	static class Traceback extends UnimplementedFunction {
		// TODO
		public Traceback() {
			super("debug.traceback");
		}
	}

}
