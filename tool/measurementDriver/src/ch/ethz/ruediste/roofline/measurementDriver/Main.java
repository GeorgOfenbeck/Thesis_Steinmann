package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommand;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class Main {

	public static void main(String args[]) throws IOException {

		String commandName = args[0];

		Injector injector = Guice.createInjector(new MainModule());

		// make the injector available to the instantiator
		Instantiator instantiator = injector.getInstance(Instantiator.class);
		instantiator.setInjector(injector);

		// get the command
		ICommand command = null;
		try {
			command = injector
					.getInstance(Key.get(ICommand.class,
							Names.named(commandName)));
		} catch (ConfigurationException e) {
			System.out
					.printf("Could not find the command named %s\nAvailable Commands:\n",
							commandName);
			instantiator.listNamed(ICommand.class);
			System.exit(1);
		}

		// execute the command
		command.execute(args);

	}
}
