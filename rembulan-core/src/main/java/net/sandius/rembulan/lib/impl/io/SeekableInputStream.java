package net.sandius.rembulan.lib.impl.io;

import net.sandius.rembulan.util.Check;

import java.io.IOException;
import java.io.InputStream;

public class SeekableInputStream extends InputStream implements SeekableStream {

	private final InputStream in;
	private long position;

	public SeekableInputStream(InputStream in) {
		this.in = Check.notNull(in);
		this.position = 0L;
	}

	@Override
	public int read() throws IOException {
		int result = in.read();
		if (result != -1) {
			position += 1;
		}
		return result;
	}

	@Override
	public long getPosition() {
		return position;
	}

	@Override
	public long setPosition(long newPosition) {
		position = newPosition;
		return position;
	}

	@Override
	public long addPosition(long offset) {
		long newPosition = position + offset;
		if (position < 0) {
			throw new IllegalArgumentException("Illegal argument");
		}
		else {
			return setPosition(newPosition);
		}
	}

}
