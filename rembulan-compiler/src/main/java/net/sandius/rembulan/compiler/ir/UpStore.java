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

public class UpStore extends BodyNode {

	private final UpVar uv;
	private final Val src;

	public UpStore(UpVar uv, Val src) {
		this.uv = Check.notNull(uv);
		this.src = Check.notNull(src);
	}

	public UpVar upval() {
		return uv;
	}

	public Val src() {
		return src;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
