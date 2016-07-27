package net.sandius.rembulan.core;

public abstract class ChunkLoader {

	public abstract Function loadTextChunk(Variable env, String chunkName, String chunk) throws LoaderException;

	public abstract Function loadBinaryChunk(Variable env, String chunkName, byte[] chunk, int offset, int len) throws LoaderException;

}
