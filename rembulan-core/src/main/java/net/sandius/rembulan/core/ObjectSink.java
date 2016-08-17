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

public abstract class ObjectSink {

	protected boolean tailCall;
	protected Object tailCallTarget;

	protected ObjectSink() {
		tailCall = false;
		tailCallTarget = null;
	}

	// return the size of the arg-list part
	public abstract int size();

	public boolean isTailCall() {
		return tailCall;
	}

	// resets tail call to false, size to 0
	public abstract void reset();

	protected void resetTailCall() {
		tailCall = false;
		tailCallTarget = null;
	}

	public void setTailCallTarget(Object target) {
		tailCall = true;
		tailCallTarget = target;
	}

	public Object getTailCallTarget() {
		return tailCallTarget;
	}

	public abstract void push(Object o);

	public void setTo() {
		reset();
	}

	public void setTo(Object a) {
		reset();
		push(a);
	}

	public void setTo(Object a, Object b) {
		reset();
		push(a);
		push(b);
	}

	public void setTo(Object a, Object b, Object c) {
		reset();
		push(a);
		push(b);
		push(c);
	}

	public void setTo(Object a, Object b, Object c, Object d) {
		reset();
		push(a);
		push(b);
		push(c);
		push(d);
	}

	public void setTo(Object a, Object b, Object c, Object d, Object e) {
		reset();
		push(a);
		push(b);
		push(c);
		push(d);
		push(e);
	}

	public void setToArray(Object[] a) {
		reset();
		for (Object o : a) {
			push(o);
		}
	}

	public void tailCall(Object target) {
		setTo();
		setTailCallTarget(target);
	}

	public void tailCall(Object target, Object arg1) {
		setTo(arg1);
		setTailCallTarget(target);
	}

	public void tailCall(Object target, Object arg1, Object arg2) {
		setTo(arg1, arg2);
		setTailCallTarget(target);
	}

	public void tailCall(Object target, Object arg1, Object arg2, Object arg3) {
		setTo(arg1, arg2, arg3);
		setTailCallTarget(target);
	}

	public void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		setTo(arg1, arg2, arg3, arg4);
		setTailCallTarget(target);
	}

	public void tailCall(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		setTo(arg1, arg2, arg3, arg4, arg5);
		setTailCallTarget(target);
	}

	public void tailCall(Object target, Object[] args) {
		setToArray(args);
		setTailCallTarget(target);
	}

	public abstract Object[] toArray();

	public abstract Object get(int idx);

	public Object _0() {
		return get(0);
	}

	public Object _1() {
		return get(1);
	}

	public Object _2() {
		return get(2);
	}

	public Object _3() {
		return get(3);
	}

	public Object _4() {
		return get(4);
	}

}
