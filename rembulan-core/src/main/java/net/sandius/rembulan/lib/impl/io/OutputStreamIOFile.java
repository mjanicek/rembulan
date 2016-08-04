package net.sandius.rembulan.lib.impl.io;

import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.lib.impl.IOFile;
import net.sandius.rembulan.util.Check;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamIOFile extends IOFile {

	private final SeekableOutputStream out;

	public OutputStreamIOFile(OutputStream out, Table metatable, Object userValue) {
		super(metatable, userValue);
		this.out = new SeekableOutputStream(Check.notNull(out));
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	public void close() throws IOException {
		throw new UnsupportedOperationException("cannot close standard file");
	}

	public void flush() throws IOException {
		out.flush();
	}

	public void write(String s) throws IOException {
		out.write(s.getBytes());
	}

	@Override
	public long seek(IOFile.Whence whence, long offset) throws IOException {
		switch (whence) {
			case BEGINNING:
			case END:
				return out.setPosition(offset);

			case CURRENT_POSITION:
				return out.addPosition(offset);

			default: throw new IllegalArgumentException("Illegal whence: " + whence);
		}
	}

}
