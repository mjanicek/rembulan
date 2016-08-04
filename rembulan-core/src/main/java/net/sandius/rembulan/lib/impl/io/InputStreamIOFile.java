package net.sandius.rembulan.lib.impl.io;

import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.lib.impl.IOFile;
import net.sandius.rembulan.util.Check;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamIOFile extends IOFile {

	private final SeekableInputStream in;

	public InputStreamIOFile(InputStream in, Table metatable, Object userValue) {
		super(metatable, userValue);
		this.in = new SeekableInputStream(Check.notNull(in));
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public void close() throws IOException {
		throw new UnsupportedOperationException("cannot close standard file");
	}

	@Override
	public void flush() throws IOException {
		// no-op
	}

	@Override
	public void write(String s) throws IOException {
		throw new UnsupportedOperationException("Bad file descriptor");
	}

	@Override
	public long seek(IOFile.Whence whence, long offset) throws IOException {
		switch (whence) {
			case BEGINNING:
			case END:
				return in.setPosition(offset);

			case CURRENT_POSITION:
				return in.addPosition(offset);

			default: throw new IllegalArgumentException("Illegal whence: " + whence);
		}
	}

}
