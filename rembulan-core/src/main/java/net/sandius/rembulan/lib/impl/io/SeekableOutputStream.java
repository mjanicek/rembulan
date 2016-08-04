package net.sandius.rembulan.lib.impl.io;

import net.sandius.rembulan.util.Check;

import java.io.IOException;
import java.io.OutputStream;

public class SeekableOutputStream extends OutputStream implements SeekableStream {

	private final OutputStream out;
	private long position;

	public SeekableOutputStream(OutputStream out) {
		this.out = Check.notNull(out);
		this.position = 0L;
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
		position += 1;
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
