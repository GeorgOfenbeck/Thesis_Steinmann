package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommand;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class Main {

	public static void main(String args[]) throws IOException {

		/*
		 * if (args.length != 1 && args.length != 2) { System.out
		 * .println("Usage: measuringDriver measurementName [outputName]");
		 * System.exit(1); }
		 */

		String commandName = args[0];

		Injector injector = Guice.createInjector(new MainModule());

		// make the injector available to the instantiator
		injector.getInstance(Instantiator.class).setInjector(injector);

		ICommand command = injector
				.getInstance(Key.get(ICommand.class,
						Names.named(commandName)));

		command.execute(args);

	}
}
