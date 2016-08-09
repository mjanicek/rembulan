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

package net.sandius.rembulan.compiler;

import net.sandius.rembulan.util.Check;

public class CompilerSettings {

	public enum CPUAccountingMode {
		NO_CPU_ACCOUNTING,
		IN_EVERY_BASIC_BLOCK
	}

	public static final CPUAccountingMode DEFAULT_CPU_ACCOUNTING_MODE = CPUAccountingMode.IN_EVERY_BASIC_BLOCK;
	public static final boolean DEFAULT_CONST_FOLDING_MODE = true;
	public static final boolean DEFAULT_CONST_CACHING_MODE = true;

	private final CPUAccountingMode cpuAccountingMode;
	private final boolean constFolding;
	private final boolean constCaching;

	CompilerSettings(
			CPUAccountingMode cpuAccountingMode,
			boolean constFolding,
			boolean constCaching) {

		this.cpuAccountingMode = Check.notNull(cpuAccountingMode);
		this.constFolding = constFolding;
		this.constCaching = constCaching;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CompilerSettings that = (CompilerSettings) o;

		return this.cpuAccountingMode == that.cpuAccountingMode
				&& this.constFolding == that.constFolding
				&& this.constCaching == that.constCaching;
	}

	@Override
	public int hashCode() {
		int result = cpuAccountingMode.hashCode();
		result = 31 * result + (constFolding ? 1 : 0);
		result = 31 * result + (constCaching ? 1 : 0);
		return result;
	}

	public static CompilerSettings defaultSettings() {
		return new CompilerSettings(
				DEFAULT_CPU_ACCOUNTING_MODE,
				DEFAULT_CONST_FOLDING_MODE,
				DEFAULT_CONST_CACHING_MODE);
	}

	public CPUAccountingMode cpuAccountingMode() {
		return cpuAccountingMode;
	}

	public boolean constFolding() {
		return constFolding;
	}

	public boolean constCaching() {
		return constCaching;
	}

	public CompilerSettings withCPUAccountingMode(CPUAccountingMode mode) {
		return mode != this.cpuAccountingMode
				? new CompilerSettings(mode, constFolding, constCaching)
				: this;
	}

	public CompilerSettings withConstFolding(boolean mode) {
		return mode != this.constFolding
				? new CompilerSettings(cpuAccountingMode, mode, constCaching)
				: this;
	}

	public CompilerSettings withConstCaching(boolean mode) {
		return mode != this.constCaching
				? new CompilerSettings(cpuAccountingMode, constFolding, mode)
				: this;
	}

}
