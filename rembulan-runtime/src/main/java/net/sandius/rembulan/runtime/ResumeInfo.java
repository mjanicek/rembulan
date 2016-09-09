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

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.util.Check;

class ResumeInfo {

	public final Resumable resumable;
	public final Object savedState;

	public ResumeInfo(Resumable resumable, Object savedState) {
		this.resumable = Check.notNull(resumable);
		this.savedState = savedState;
	}

	public boolean resume(ExecutionContext context, Throwable error) throws ResolvedControlThrowable {
		if (error == null) {
			// no errors
			resumable.resume(context, savedState);
			Dispatch.evaluateTailCalls(context);
			return true;
		}
		else {
			// there is an error to be handled
			if (resumable instanceof ProtectedResumable) {
				// top is protected, can handle the error
				ProtectedResumable pr = (ProtectedResumable) resumable;
				pr.resumeError(context, savedState, Conversions.toErrorObject(error));
				Dispatch.evaluateTailCalls(context);
				return true;
			}
			else {
				// top is not protected, continue unwinding the stack
				return false;
			}
		}
	}

}
