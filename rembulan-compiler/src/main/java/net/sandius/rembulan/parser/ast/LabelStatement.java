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

import java.util.Objects;

public class LabelStatement extends BodyStatement {

	private final Name labelName;

	public LabelStatement(Attributes attr, Name labelName) {
		super(attr);
		this.labelName = Objects.requireNonNull(labelName);
	}

	public Name labelName() {
		return labelName;
	}

	public LabelStatement update(Name labelName) {
		if (this.labelName.equals(labelName)) {
			return this;
		}
		else {
			return new LabelStatement(attributes(), labelName);
		}
	}

	public LabelStatement withAttributes(Attributes attr) {
		if (attributes().equals(attr)) return this;
		else return new LabelStatement(attr, labelName);
	}

	public LabelStatement with(Object o) {
		return this.withAttributes(attributes().with(o));
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
