package net.sandius.rembulan.compiler;

import net.sandius.rembulan.parser.ast.Chunk;

public class IRTranslator {

	public static Module translate(Chunk chunk) {
		ModuleBuilder moduleBuilder = new ModuleBuilder();
		IRTranslatorTransformer translator = new IRTranslatorTransformer(moduleBuilder);
		translator.transform(chunk);
		return moduleBuilder.build();
	}

}
