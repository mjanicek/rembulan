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

package net.sandius.rembulan.impl;

import net.sandius.rembulan.SchedulingContext;
import net.sandius.rembulan.util.Check;

/**
 * Static factory for instantiating scheduling contexts.
 */
public final class SchedulingContexts {

	private SchedulingContexts() {
		// not to be instantiated
	}

	private static final SchedulingContext NEVER_INSTANCE = new Never();
	private static final SchedulingContext ALWAYS_INSTANCE = new Always();

	/**
	 * Returns a scheduling context that always returns {@code false} from
	 * {@link SchedulingContext#shouldYield()}, i.e., that <i>never</i> indicates
	 * that the caller should yield.
	 *
	 * @return  a scheduling context that never indicates that the caller should yield
	 */
	public static SchedulingContext never() {
		return NEVER_INSTANCE;
	}

	/**
	 * Returns a scheduling context that always returns {@code false} from
	 * {@link SchedulingContext#shouldYield()}, i.e., that <i>always</i> indicates
	 * that the caller should yield.
	 *
	 * @return  a scheduling context that always indicates that the caller should yield
	 */
	public static SchedulingContext always() {
		return ALWAYS_INSTANCE;
	}

	/**
	 * Returns a scheduling context with an internal tick counter (initialised to
	 * {@code max}).
	 *
	 * <p>Every call to {@link SchedulingContext#registerTicks(int)} with
	 * a positive argument decreases the counter accordingly. The scheduling context
	 * returns {@code true} from {@link SchedulingContext#shouldYield()} if and only if
	 * counter is lesser than or equal to 0.</p>
	 *
	 * @param max  the initial counter value, must be non-negative
	 * @return  a scheduling context that starts indicating that the caller should yield
	 *          once the counter is lesser than or equal to 0
	 *
	 * @throws IllegalArgumentException  when {@code max} is negative
	 */
	public static SchedulingContext upTo(long max) {
		return new UpTo(max);
	}

	private static class Never implements SchedulingContext {

		@Override
		public void registerTicks(int ticks) {
			// no-op
		}

		@Override
		public boolean shouldYield() {
			return false;
		}

	}

	private static class Always implements SchedulingContext {

		@Override
		public void registerTicks(int ticks) {
			// no-op
		}

		@Override
		public boolean shouldYield() {
			return true;
		}

	}

	private static class UpTo implements SchedulingContext {

		private long allowance;

		public UpTo(long max) {
			Check.nonNegative(max);
			this.allowance = max;
		}

		@Override
		public void registerTicks(int ticks) {
			allowance -= Math.max(0, ticks);
		}

		@Override
		public boolean shouldYield() {
			return allowance <= 0;
		}

	}

}
