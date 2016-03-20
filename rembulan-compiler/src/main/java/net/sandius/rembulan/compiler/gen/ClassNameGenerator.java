package net.sandius.rembulan.compiler.gen;

public interface ClassNameGenerator {

	String next();

	ClassNameGenerator childGenerator();

}
