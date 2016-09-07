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

package net.sandius.rembulan.runtime;

/**
 * An interface to the cooperative scheduler used in executing Lua programs.
 *
 * <p>The interface specifies two methods: {@link #registerTicks(int)} and {@link #shouldYield()}.
 * The former is used by an {@link ExecutionContext} to register ticks spent in executing
 * a Lua program; the latter is used by ExecutionContexts to determine whether the execution
 * should be paused.</p>
 *
 * <p>For basic implementations of this interface, see the utility class
 * {@link net.sandius.rembulan.impl.SchedulingContexts}.</p>
 */
public interface SchedulingContext {

	/**
	 * Informs the scheduler that the current task is about to consume or has consumed
	 * {@code ticks} virtual ticks.
	 *
	 * <p>This method only registers {@code ticks} with the scheduler, irrespective
	 * of whether the current task should yield control and pause. In order to check whether
	 * control should be yielded, use {@link #shouldYield()}.</p>
	 *
	 * <p>It is the responsibility of this method's implementation to check that {@code ticks}
	 * is a sensible value &mdash; typically, only positive or non-negative values make sense
	 * &mdash; and handle them either by ignoring non-sensible values or throwing an exception.</p>
	 *
	 * <p><b>Note</b>: this method is not meant to be used directly by Lua programs, but rather
	 * to be called by the implementations of {@link ExecutionContext}.</p>
	 *
	 * @param ticks  number of ticks to be registered with the scheduler
	 */
	void registerTicks(int ticks);

	/**
	 * Polls the scheduler, returning {@code true} if the current task should
	 * yield control and pause.
	 *
	 * <p><b>Note</b>: this method is not meant to be used directly by Lua programs, but rather
	 * to be called by the implementations of {@link ExecutionContext}.</p>
	 *
	 * @return  {@code true} if the current task should yield
	 */
	boolean shouldYield();

}
