package net.sandius.rembulan.lbc;

public interface PrototypeReader {

	Prototype load(String program) throws PrototypeReaderException;

}
