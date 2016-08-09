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

package net.sandius.rembulan.compiler.analysis.types;

public abstract class LuaTypes {

	private LuaTypes() {
		// not to be instantiated
	}

	static AbstractType abstractSubtype(AbstractType parent, String name) {
		return new AbstractType(parent, name);
	}

	static ConcreteType concreteSubtype(AbstractType parent, String name) {
		return new ConcreteType(parent, name);
	}

	static <T> ConcreteLitType<T> concreteSubtype(AbstractType parent, String name, Class<T> clazz) {
		return new ConcreteLitType<>(parent, name);
	}

	public static final TopType ANY = new TopType("any");

	public static final DynamicType DYNAMIC = new DynamicType("dynamic");

	public static final ConcreteType NIL = concreteSubtype(ANY, "nil");
	public static final AbstractType NON_NIL = abstractSubtype(ANY, "nonnil");

	public static final ConcreteLitType<Boolean> BOOLEAN = concreteSubtype(NON_NIL, "boolean", Boolean.class);
	public static final AbstractType NUMBER = abstractSubtype(NON_NIL, "number");
	public static final ConcreteLitType<Long> NUMBER_INTEGER = concreteSubtype(NUMBER, "integer", Long.class);
	public static final ConcreteLitType<Double> NUMBER_FLOAT = concreteSubtype(NUMBER, "float", Double.class);
	public static final ConcreteLitType<String> STRING = concreteSubtype(NON_NIL, "string", String.class);
	public static final ConcreteType TABLE = concreteSubtype(NON_NIL, "table");

	public static final AbstractType FUNCTION = abstractSubtype(NON_NIL, "function");

	public static FunctionType functionType(TypeSeq argTypes, TypeSeq returnTypes) {
		return new FunctionType(FUNCTION, argTypes, returnTypes);
	}

}
