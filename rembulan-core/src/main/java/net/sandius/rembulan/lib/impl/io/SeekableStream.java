package net.sandius.rembulan.lib.impl.io;

public interface SeekableStream {

	long getPosition();

	long setPosition(long newPosition);

	long addPosition(long offset);

}
