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

import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.exec.CallInitialiser;
import net.sandius.rembulan.exec.Continuation;
import net.sandius.rembulan.impl.ReturnBuffers;

import java.util.Objects;

/**
 * The default implementation of a call initialiser.
 *
 * <p>This class serves as a bridge between call executors defined in the package
 * {@link net.sandius.rembulan.exec} and the runtime implementation.</p>
 */
public class RuntimeCallInitialiser implements CallInitialiser {

	private final StateContext stateContext;
	private final ReturnBufferFactory returnBufferFactory;

	RuntimeCallInitialiser(StateContext stateContext, ReturnBufferFactory returnBufferFactory) {
		this.stateContext = Objects.requireNonNull(stateContext);
		this.returnBufferFactory = Objects.requireNonNull(returnBufferFactory);
	}

	/**
	 * Returns a new call initialiser for calls executed in the specified state
	 * context {@code stateContext} that use return buffers initialised by the specified
	 * factory {@code returnBufferFactory}.
	 *
	 * @param stateContext  the state context, must not be {@code null}
	 * @param returnBufferFactory  the return buffer factory, must not be {@code null}
	 * @return  a new call initialiser for {@code stateContext}
	 *
	 * @throws NullPointerException  if {@code stateContext} or {@code returnBufferFactory}
	 *                               is {@code null}
	 */
	public static RuntimeCallInitialiser forState(StateContext stateContext, ReturnBufferFactory returnBufferFactory) {
		return new RuntimeCallInitialiser(stateContext, returnBufferFactory);
	}

	/**
	 * Returns a new call initialiser for calls executed in the specified state
	 * context {@code stateContext}, and using the default return buffer factory
	 * (see {@link ReturnBuffers#defaultFactory()}).
	 *
	 * @param stateContext  the state context, must not be {@code null}
	 * @return  a new call initialiser for {@code stateContext}
	 *
	 * @throws NullPointerException  if {@code stateContext} is {@code null}
	 */
	public static RuntimeCallInitialiser forState(StateContext stateContext) {
		return forState(stateContext, ReturnBuffers.defaultFactory());
	}

	@Override
	public Continuation newCall(Object fn, Object... args) {
		return Call.init(stateContext, returnBufferFactory, fn, args).getCurrentContinuation();
	}

}
