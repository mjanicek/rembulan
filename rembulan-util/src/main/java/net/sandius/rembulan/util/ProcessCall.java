package net.sandius.rembulan.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

	private static String drainToString(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuilder resultBuilder = new StringBuilder();
		do {
			String line = reader.readLine();
			if (line != null) {
				if (resultBuilder.length() > 0) {
					// we don't want the trailing newline
					resultBuilder.append('\n');
				}
				resultBuilder.append(line);
			}
			else {
				break;
			}
		} while (true);

		// got non-empty stderr
		if (resultBuilder.length() > 0) {
			return resultBuilder.toString();
		}
		else {
			return null;
		}
	}

	public String drainStdoutToString() throws IOException {
		return drainToString(out);
	}

	public String drainStderrToString() throws IOException {
		return drainToString(err);
	}

}
