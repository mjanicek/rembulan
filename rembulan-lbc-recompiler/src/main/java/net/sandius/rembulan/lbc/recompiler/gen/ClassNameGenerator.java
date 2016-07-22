package net.sandius.rembulan.lbc.recompiler.gen;

public interface ClassNameGenerator {

	String next();

	ClassNameGenerator childGenerator();

}
