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

package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.ByteVector;

import java.util.Objects;

public class CompiledClass {

	protected final String name;
	protected final ByteVector bytes;

	public CompiledClass(String name, ByteVector bytes) {
		this.name = Objects.requireNonNull(name);
		this.bytes = Objects.requireNonNull(bytes);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CompiledClass that = (CompiledClass) o;

		return name.equals(that.name) && bytes.equals(that.bytes);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + bytes.hashCode();
		return result;
	}

	public String name() {
		return name;
	}

	public ByteVector bytes() {
		return bytes;
	}

}
