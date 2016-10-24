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

package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.ByteString;

import java.util.Objects;

public abstract class LoadConst extends BodyNode {

	private final Val dest;

	private LoadConst(Val dest) {
		this.dest = Objects.requireNonNull(dest);
	}

	public Val dest() {
		return dest;
	}

	public static class Nil extends LoadConst {

		public Nil(Val dest) {
			super(dest);
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

	}
	
	public static class Bool extends LoadConst {
		
		private final boolean value;
		
		public Bool(Val dest, boolean value) {
			super(dest);
			this.value = value;
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

		public boolean value() {
			return value;
		}

	}

	public static class Int extends LoadConst {

		private final long value;

		public Int(Val dest, long value) {
			super(dest);
			this.value = value;
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

		public long value() {
			return value;
		}

	}

	public static class Flt extends LoadConst {

		private final double value;

		public Flt(Val dest, double value) {
			super(dest);
			this.value = value;
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

		public double value() {
			return value;
		}

	}

	public static class Str extends LoadConst {

		private final ByteString value;

		public Str(Val dest, ByteString value) {
			super(dest);
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public void accept(IRVisitor visitor) {
			visitor.visit(this);
		}

		public ByteString value() {
			return value;
		}

	}

}
