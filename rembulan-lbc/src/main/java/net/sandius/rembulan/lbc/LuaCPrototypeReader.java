package net.sandius.rembulan.lbc;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ProcessCall;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuaCPrototypeReader implements PrototypeReader {

	public final String luacName;

	public LuaCPrototypeReader(String luacName) {
		this.luacName = luacName;
	}

	public String getVersion() {
		List<String> args = new ArrayList<String>();
		args.add("-v");

		try {
			ProcessCall call = ProcessCall.doCall(luacName, args);
			call.in.close();
			return call.drainStdoutToString();
		}
		catch (IOException e) {
			return null;
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

		ProcessCall call = ProcessCall.doCall(luacName, args);

		call.in.write(program.getBytes());
		call.in.close();

		// read stderr
		String error = call.drainStderrToString();
		if (error != null) {
			throw new IllegalArgumentException(error);
		}

		// no error, read stdout
		PrototypeLoader.fromInputStream(new BufferedInputStream(call.out)).accept(pv);
	}

	@Override
	public Prototype load(String program) throws PrototypeReaderException {
		PrototypeBuilderVisitor visitor = new PrototypeBuilderVisitor();
		try {
			accept(program, visitor);
			return visitor.get();
		}
		catch (IOException ex) {
			throw new PrototypeReaderException(ex);
		}
	}

}
