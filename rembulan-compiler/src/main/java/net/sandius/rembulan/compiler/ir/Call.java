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

import net.sandius.rembulan.util.Check;

public class Call extends BodyNode {

	private final MultiVal dest;
	private final Val fn;
	private final VList args;

	public Call(MultiVal dest, Val fn, VList args) {
		this.dest = Check.notNull(dest);
		this.fn = Check.notNull(fn);
		this.args = Check.notNull(args);
	}

	public MultiVal dest() {
		return dest;
	}

	public Val fn() {
		return fn;
	}

	public VList args() {
		return args;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
