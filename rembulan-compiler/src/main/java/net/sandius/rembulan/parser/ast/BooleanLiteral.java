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

public class BooleanLiteral extends Literal {

	public static final BooleanLiteral TRUE = new BooleanLiteral(true);
	public static final BooleanLiteral FALSE = new BooleanLiteral(false);

	private final boolean value;

	private BooleanLiteral(boolean value) {
		this.value = value;
	}

	public BooleanLiteral fromBoolean(boolean b) {
		return b ? TRUE : FALSE;
	}

	public boolean value() {
		return value;
	}

	@Override
	public Literal accept(Transformer tf) {
		return tf.transform(this);
	}

}
