package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class LuaCPrototypeLoader {

	public final String luacName;

	public LuaCPrototypeLoader(String luacName) {
		this.luacName = luacName;
	}

	private class Call {

		public final OutputStream in;
		public final InputStream out;
		public final InputStream err;

		public Call(String[] args) throws IOException {
			Check.notNull(args);
			ProcessBuilder builder = new ProcessBuilder(args);

			builder.redirectErrorStream(false);

			Process p = builder.start();

			this.in = p.getOutputStream();
			this.out = p.getInputStream();
			this.err = p.getErrorStream();
		}

	}

	public Prototype load(String program, String name) throws IOException {
		Check.notNull(name);
		Check.notNull(program);

		Call call = new Call(new String[] { luacName, "-o", "-", "-" });

		call.in.write(program.getBytes());
		call.in.close();

		// read stderr
		BufferedReader errStream = new BufferedReader(new InputStreamReader(call.err));

		String s = null;
		String errString = "";

		do {
			s = errStream.readLine();
			if (s != null) {
				if (errString.isEmpty()) {
					errString += "\n";
				}
				errString += s;
			}
		} while (s != null);

		if (!errString.isEmpty()) {
			throw new IllegalArgumentException(errString);
		}

		// no error, read stdout
		InputStream outStream = new BufferedInputStream(call.out);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];

		int n = 0;
		do {
			n = outStream.read(buf, 0, buf.length);
			if (n > 0) {
				baos.write(buf, 0, n);
			}
		} while (n >= 0);

		// compiled prototype from luac
		byte[] bytes = baos.toByteArray();

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		return PrototypeLoader.undump(bais, name, true);
	}

}
