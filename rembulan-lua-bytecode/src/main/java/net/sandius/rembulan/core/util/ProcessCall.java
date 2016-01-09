package net.sandius.rembulan.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcessCall {

	public final Process process;

	public final OutputStream in;
	public final InputStream out;
	public final InputStream err;

	private ProcessCall(List<String> commandLineArgs) throws IOException {
		Objects.requireNonNull(commandLineArgs);

		ProcessBuilder builder = new ProcessBuilder(commandLineArgs);

		builder.redirectErrorStream(false);

		this.process = builder.start();

		this.in = process.getOutputStream();
		this.out = process.getInputStream();
		this.err = process.getErrorStream();
	}

	public static ProcessCall doCall(List<String> commandLineArgs) throws IOException {
		return new ProcessCall(commandLineArgs);
	}

	public static ProcessCall doCall(String program, List<String> args) throws IOException {
		Objects.requireNonNull(program);

		ArrayList<String> commandLineArgs = new ArrayList<>();
		commandLineArgs.add(program);
		commandLineArgs.addAll(args);

		return new ProcessCall(commandLineArgs);
	}

}
