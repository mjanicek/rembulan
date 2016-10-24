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

package net.sandius.rembulan.lib.luajava;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.AbstractLibFunction;
import net.sandius.rembulan.lib.ArgumentIterator;
import net.sandius.rembulan.lib.SimpleLoaderFunction;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.lang.reflect.InvocationTargetException;

public final class LuaJavaLib {

	private LuaJavaLib() {
		// not to be instantiated
	}

	public static LuaFunction loader(Table env) {
		return new LoaderFunction(env);
	}

	static class LoaderFunction extends SimpleLoaderFunction {

		public LoaderFunction(Table env) {
			super(env);
		}

		@Override
		public Object install(StateContext context, Table env, ByteString modName, ByteString origin) {
			Table t = context.newTable();

			t.rawset("newInstance", LuaJavaLib.NewInstance.INSTANCE);
			t.rawset("bindClass", LuaJavaLib.BindClass.INSTANCE);
			t.rawset("new", LuaJavaLib.New.INSTANCE);
			t.rawset("createProxy", new UnimplementedFunction(modName + ".createProxy"));
			t.rawset("loadLib", new UnimplementedFunction(modName + ".loadLib"));

			env.rawset(modName, t);

			return t;
		}

	}

	/**
	 * {@code newInstance(className, ...)}
	 *
	 * <p>This function creates a new Java object, and returns a Lua object that is a reference
	 * to the actual Java object. You can access this object with the regular syntax used
	 * to access object oriented functions in Lua objects.</p>
	 *
	 * <p>The first parameter is the name of the class to be instantiated. The other parameters
	 * are passed to the Java Class constructor.</p>
	 *
	 * <p>Example:</p>
	 *
	 * <pre>
	 * obj = luajava.newInstance("java.lang.Object")
	 * -- obj is now a reference to the new object
	 * -- created and any of its methods can be accessed.
	 *
	 * -- this creates a string tokenizer to the "a,b,c,d"
	 * -- string using "," as the token separator.
	 * strTk = luajava.newInstance("java.util.StringTokenizer",
	 *     "a,b,c,d", ",")
	 * while strTk:hasMoreTokens() do
	 *     print(strTk:nextToken())
	 * end
	 * </pre>
	 *
	 * <p>The code above should print the following on the screen:</p>
	 *
	 * <pre>
	 * a
	 * b
	 * c
	 * d
	 * </pre>
	 */
	static class NewInstance extends AbstractLibFunction {

		static final NewInstance INSTANCE = new NewInstance();

		@Override
		protected String name() {
			return "newInstance";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args)
				throws ResolvedControlThrowable {

			String className = args.nextString().toString();
			Object[] ctorArgs = args.copyRemaining();

			final ObjectWrapper instance;
			try {
				instance = ObjectWrapper.newInstance(className, ctorArgs);
			}
			catch (ClassNotFoundException | MethodSelectionException | IllegalAccessException
					| InstantiationException | InvocationTargetException ex) {

				throw new LuaRuntimeException(ex);
			}

			context.getReturnBuffer().setTo(instance);
		}

	}

	/**
	 * {@code bindClass(className)}
	 *
	 * <p>This function retrieves a Java class corresponding to {@code className}.
	 * The returned object can be used to access static fields and methods of the corresponding
	 * class.</p>
	 *
	 * <p>Example:</p>
	 *
	 * <pre>
	 * sys = luajava.bindClass("java.lang.System")
	 * print ( sys:currentTimeMillis() )
	 *
	 * -- this prints the time returned by the function.
	 * </pre>
	 */
	static class BindClass extends AbstractLibFunction {

		static final BindClass INSTANCE = new BindClass();

		@Override
		protected String name() {
			return "bindClass";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			String className = args.nextString().toString();

			final ClassWrapper wrapper;
			try {
				wrapper = ClassWrapper.of(className);
			}
			catch (ClassNotFoundException ex) {
				throw new LuaRuntimeException(ex);
			}

			context.getReturnBuffer().setTo(wrapper);
		}

	}

	/**
	 * {@code new(javaClass)}
	 *
	 * <p>This function receives a java.lang.Class and returns a new instance of this class.</p>
	 *
	 * <p>{@code new} works just like {@code newInstance}, but the first argument is an instance of the
	 * class.</p>
	 *
	 * <p>Example:</p>
	 *
	 * <pre>
	 * str = luajava.bindClass("java.lang.String")
	 * strInstance = luajava.new(str)
	 * </pre>
	 */
	static class New extends AbstractLibFunction {

		static final New INSTANCE = new New();

		@Override
		protected String name() {
			return "new";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ClassWrapper classWrapper = args.nextUserdata(ClassWrapper.staticTypeName(), ClassWrapper.class);

			final ObjectWrapper instance;
			try {
				instance = ObjectWrapper.newInstance(classWrapper.get(), new Object[] { });
			}
			catch (MethodSelectionException | IllegalAccessException | InstantiationException
					| InvocationTargetException ex) {
				throw new LuaRuntimeException(ex);
			}

			context.getReturnBuffer().setTo(instance);
		}

	}

	/**
	 * {@code createProxy(interfaceNames, luaObject)}
	 *
	 * <p>We can also, instead of creating a Java object to be manipulated by Lua, create a Lua
	 * object that will be manipulated by Java. We can do that in LuaJava by creating a proxy to
	 * that object. This is done by the {@code createProxy} function.</p>
	 *
	 * <p>The function {@code createProxy} returns a java Object reference that can be used as
	 * an implementation of the given interface.</p>
	 *
	 * <p>{@code createProxy} receives a string that contains the names of the interfaces to
	 * be implemented, separated by a comma ({@code ,}), and a Lua object that is the interface
	 * implementation.</p>
	 *
	 * <p>Example:</p>
	 *
	 * <pre>
	 * button = luajava.newInstance("java.awt.Button", "execute")
	 * button_cb = {}
	 * function button_cb.actionPerformed(ev)
	 *   -- ...
	 * end
	 *
	 * buttonProxy = luajava.createProxy("java.awt.ActionListener",
	 *    button_cb)
	 *
	 * button:addActionListener(buttonProxy)
	 * </pre>
	 *
	 * <p>We can use Lua scripts to write implementations only for Java interfaces.</p>
	 */
	static class CreateProxy extends AbstractLibFunction {

		static final CreateProxy INSTANCE = new CreateProxy();

		@Override
		protected String name() {
			return "createProxy";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			throw new UnsupportedOperationException("not implemented: " + name());  // TODO
		}

	}

	/**
	 * {@code loadLib(className, methodName)}
	 *
	 * <p>loadLib is a function that has a use similar to Lua's {@code loadlib} function.
	 * The purpose of this function is to allow users to write libraries in Java and then load
	 * them into Lua.</p>
	 *
	 * <p>What {@code loadLib} does is call a static function in a given class and execute
	 * a given method, which should receive {@code LuaState} as parameter. If this function
	 * returns a integer, LuaJava takes it as the number of parameters returned by the the
	 * function, otherwise nothing is returned.</p>
	 *
	 * <p>The following Lua example can access the global {@code eg} created by the Java class
	 * {@code test.LoadLibExample}:
	 *
	 * <pre>
	 * luajava.loadLib("test.LoadLibExample", "open")
	 * eg.example(3)
	 * </pre>
	 *
	 * <p>And this Java example implements the method {@code example}:</p>
	 *
	 * <pre>
	 * public static int open(LuaState L) throws LuaException {
	 *     L.newTable();
	 *     L.pushValue(-1);
	 *     L.setGlobal("eg");
	 *
	 *     L.pushString("example");
	 *
	 *     L.pushJavaFunction(new JavaFunction(L) {
	 *         //
	 *         // Example for loadLib.
	 *         // Prints the time and the first parameter, if any.
	 *         //
	 *         public int execute() throws LuaException {
	 *             System.out.println(new Date().toString());
	 *
	 *             if (L.getTop() > 1) {
	 *                 System.out.println(getParam(2));
	 *             }
	 *
	 *             return 0;
	 *         }
	 *     });
	 *
	 *     L.setTable(-3);
	 *
	 *     return 1;
	 * }
	 * </pre>
	 */
	static class LoadLib extends AbstractLibFunction {

		static final LoadLib INSTANCE = new LoadLib();

		@Override
		protected String name() {
			return "loadLib";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			throw new UnsupportedOperationException("not implemented: " + name());  // TODO
		}

	}

}
