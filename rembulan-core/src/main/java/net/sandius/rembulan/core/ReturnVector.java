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

package net.sandius.rembulan.core;

public interface ReturnVector {

	// return the size of the arg-list part
	int size();

	boolean isTailCall();

	Object getTailCallTarget();

	void setTo();

	void setTo(Object a);

	void setTo(Object a, Object b);

	void setTo(Object a, Object b, Object c);

	void setTo(Object a, Object b, Object c, Object d);

	void setTo(Object a, Object b, Object c, Object d, Object e);

	void setToArray(Object[] a);

	void tailCall(Object target);

	void tailCall(Object target, Object arg1);

	void tailCall(Object target, Object arg1, Object arg2);

	void tailCall(Object target, Object arg1, Object arg2, Object arg3);

	void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4);

	void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5);

	void tailCall(Object target, Object[] args);

	Object[] toArray();

	Object get(int idx);

	Object _0();

	Object _1();

	Object _2();

	Object _3();

	Object _4();

}
