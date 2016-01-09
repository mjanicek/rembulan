package net.sandius.rembulan.core;

import net.sandius.rembulan.core.util.ProcessCall;
import net.sandius.rembulan.util.Check;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LuaCPrototypeLoader {

	public final String luacName;

	public LuaCPrototypeLoader(String luacName) {
		this.luacName = luacName;
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

		ProcessCall call = ProcessCall.doCall(luacName, args);

		call.in.write(program.getBytes());
		call.in.close();

		// read stderr
		BufferedReader errStream = new BufferedReader(new InputStreamReader(call.err));
		StringBuilder errBuilder = new StringBuilder();
		do {
			String line = errStream.readLine();
			if (line != null) {
				if (errBuilder.length() > 0) {
					// we don't want the trailing newline
					errBuilder.append('\n');
				}
				errBuilder.append(line);
			}
			else {
				break;
			}
		} while (true);

		// got non-empty stderr
		if (errBuilder.length() > 0) {
			throw new IllegalArgumentException(errBuilder.toString());
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
