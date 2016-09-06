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

package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.LuaFormat;

import java.util.Objects;

public final class Name {

	private final String value;

	private Name(String value) {
		this.value = checkValidName(value);
	}

	public static Name fromString(String s) {
		return new Name(s);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Name name = (Name) o;
		return Objects.equals(value, name.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	public static String checkValidName(String s) {
		if (!LuaFormat.isValidName(s)) {
			throw new IllegalArgumentException("Not a valid name: " + s);
		}
		else {
			return s;
		}
	}

	public String value() {
		return value;
	}

}
