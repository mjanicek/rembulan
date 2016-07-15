package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.parser.ast.Chunk;
import net.sandius.rembulan.util.Check;

public class NameResolver {

	public static Chunk resolveNames(Chunk chunk) {
		Check.notNull(chunk);
		chunk = new NameResolutionTransformer().transform(chunk);
		chunk = new LabelResolutionTransformer().transform(chunk);
		return chunk;
	}

}
