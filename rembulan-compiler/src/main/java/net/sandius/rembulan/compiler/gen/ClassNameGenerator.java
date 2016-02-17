package net.sandius.rembulan.compiler.gen;

public interface ClassNameGenerator {

	String className();

	ClassNameGenerator child(int idx);

}
