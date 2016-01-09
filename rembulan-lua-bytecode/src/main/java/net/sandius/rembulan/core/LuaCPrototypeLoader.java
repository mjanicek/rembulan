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
import java.util.List;

public class LuaCPrototypeLoader {

	public final String luacName;

	public LuaCPrototypeLoader(String luacName) {
		this.luacName = luacName;
	}

	private class Call {

		public final OutputStream in;
		public final InputStream out;
		public final InputStream err;

		public Call(String program, List<String> args) throws IOException {
			Check.notNull(program);
			Check.notNull(args);

			args.add(0, program);

			ProcessBuilder builder = new ProcessBuilder(args);

			builder.redirectErrorStream(false);

			Process p = builder.start();

			this.in = p.getOutputStream();
			this.out = p.getInputStream();
			this.err = p.getErrorStream();
		}

	}

	public void accept(String program, PrototypeVisitor pv) throws IOException {
		accept(program, pv, false);
	}

	public void accept(String program, PrototypeVisitor pv, boolean stripDebug) throws IOException {
		Check.notNull(program);

		List<String> args = new ArrayList<String>();

		if (stripDebug) {
			args.add("-s");
		}

		// output goes to stdout
		args.add("-o");
		args.add("-");

		// read from stdin
		args.add("-");

		Call call = new Call(luacName, args);

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
		PrototypeLoader.fromInputStream(new BufferedInputStream(call.out)).accept(pv);
	}

	@Deprecated
	public Prototype load(String program) throws IOException {
		PrototypeBuilderVisitor visitor = new PrototypeBuilderVisitor();
		accept(program, visitor);
		return visitor.get();
	}

}
