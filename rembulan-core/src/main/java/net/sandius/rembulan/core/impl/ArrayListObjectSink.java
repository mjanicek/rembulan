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

package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.ObjectSinkFactory;

import java.util.ArrayList;

public class ArrayListObjectSink extends ObjectSink {

	public static final ObjectSinkFactory FACTORY_INSTANCE = new ObjectSinkFactory() {
		@Override
		public ObjectSink newObjectSink() {
			return new ArrayListObjectSink();
		}
	};

	private final ArrayList<Object> buf;

	public ArrayListObjectSink() {
		super();
		buf = new ArrayList<>();
	}

	@Override
	public int size() {
		return buf.size();
	}

	@Override
	public void reset() {
		buf.clear();
		resetTailCall();
	}

	@Override
	public void push(Object o) {
		buf.add(o);
	}

	@Override
	public Object[] toArray() {
		return buf.toArray();
	}

	@Override
	public Object get(int idx) {
		return idx >= 0 && idx < buf.size() ? buf.get(idx) : null;
	}

}
