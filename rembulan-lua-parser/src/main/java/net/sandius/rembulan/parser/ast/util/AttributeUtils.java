package net.sandius.rembulan.parser.ast.util;

import net.sandius.rembulan.parser.ast.Attributes;
import net.sandius.rembulan.parser.ast.SourceInfo;
import net.sandius.rembulan.parser.ast.SyntaxElement;

public abstract class AttributeUtils {

	private AttributeUtils() {
		// not to be instantiated
	}

	public static String sourceInfoString(SyntaxElement elem) {
		SourceInfo src = elem.attributes().get(SourceInfo.class);
		return src != null ? src.toString() : "?:?";
	}

}
