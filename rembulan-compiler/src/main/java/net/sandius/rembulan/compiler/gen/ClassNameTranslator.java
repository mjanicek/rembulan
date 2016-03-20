package net.sandius.rembulan.compiler.gen;

public interface ClassNameTranslator {

	String className();

	ClassNameTranslator child(int idx);

}
