/*******************************************************************************
* Copyright (c) 2009-2011 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

/**
 * Data class to hold debug information relating to local variables for a {@link Prototype}
 */
public class LocalVariable {
	/** The local variable name */
	public final String variableName;
	
	/** The instruction offset when the variable comes into scope */ 
	public final int beginPC;
	
	/** The instruction offset when the variable goes out of scope */ 
	public final int endPC;
	
	/**
	 * Construct a LocVars instance. 
	 * @param variableName The local variable name
	 * @param beginPC The instruction offset when the variable comes into scope
	 * @param endPC The instruction offset when the variable goes out of scope
	 */
	public LocalVariable(String variableName, int beginPC, int endPC) {
		Check.notNull(variableName);
		this.variableName = variableName;
		this.beginPC = beginPC;
		this.endPC = endPC;
	}
	
	public String tojstring() {
		return variableName + " " + beginPC + "-" + endPC;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LocalVariable that = (LocalVariable) o;

		return beginPC == that.beginPC && endPC == that.endPC && variableName.equals(that.variableName);
	}

	@Override
	public int hashCode() {
		int result = variableName.hashCode();
		result = 31 * result + beginPC;
		result = 31 * result + endPC;
		return result;
	}

}
