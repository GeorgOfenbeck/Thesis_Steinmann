package ch.ethz.ruediste.roofline.measurementDriver.infrastructure.services;

import java.io.*;

import org.apache.commons.exec.*;

import ch.ethz.ruediste.roofline.measurementDriver.util.NullOutputStream;

public class CommandService {

	public void runCommand(File workingDirectory, String command,
			String[] arguments) throws ExecuteException, IOException {
		runCommand(workingDirectory, command, arguments, 0, true);
	}

	public void runCommand(File workingDirectory, String command,
			String[] arguments, int desiredExitValue, boolean showOutput)
			throws ExecuteException, IOException {

		// setup command line
		CommandLine cmdLine = new CommandLine(command);
		cmdLine.addArguments(arguments);

		// setup executor
		DefaultExecutor executor = new DefaultExecutor();

		// set the output to null if the output should not be shown
		// still show the errors
		if (!showOutput) {
			executor.setStreamHandler(new PumpStreamHandler(
					new NullOutputStream(), System.err));
		}

		executor.setExitValue(desiredExitValue);
		executor.setWorkingDirectory(workingDirectory);

		// perform command
		executor.execute(cmdLine);
	}
}
