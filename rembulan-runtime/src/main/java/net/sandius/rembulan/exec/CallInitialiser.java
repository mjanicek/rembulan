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

package net.sandius.rembulan.exec;

/**
 * An interface for constructing call continuations.
 */
public interface CallInitialiser {

	/**
	 * Returns the Lua call {@code fn(args...)} reified as a continuation.
	 *
	 * @param fn  the call target, may be {@code null}
	 * @param args  call arguments, must not be {@code null}
	 * @return  a continuation representing the call {@code fn(args...)}
	 *
	 * @throws NullPointerException  if {@code args} is {@code null}
	 */
	Continuation newCall(Object fn, Object... args);

}
