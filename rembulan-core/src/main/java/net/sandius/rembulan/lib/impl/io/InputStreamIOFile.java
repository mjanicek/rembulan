package net.sandius.rembulan.lib.impl.io;

import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.lib.impl.IOFile;
import net.sandius.rembulan.util.Check;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamIOFile extends IOFile {

	private final InputStream in;
	private boolean closed;

	public InputStreamIOFile(InputStream in, Table metatable, Object userValue) {
		super(metatable, userValue);

		this.in = Check.notNull(in);
		this.closed = false;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			in.close();
			closed = true;
		}
	}

	@Override
	public void flush() throws IOException {
		// no-op
	}

	@Override
	public void write(String s) throws IOException {
		throw new UnsupportedOperationException("Bad file descriptor");
	}

}
