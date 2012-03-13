package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.*;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.SystemInfoService;
import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.repositories.ReflectionRepository;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;

import com.google.inject.*;

public class LibraryMain {
	static private Logger log = Logger.getLogger(Main.class);

	@Inject
	public Instantiator instantiator;

	@Inject
	public Configuration configuration;

	@Inject
	public RuntimeMonitor runtimeMonitor;

	@Inject
	public ReflectionRepository reflectionRepository;

	@Inject
	public MainHelper mainHelper;
	
	@Inject
	public SystemInfoService systemInfoService;

	/**
	 * Initializes the measurement driver
	 */
	public static void initialize() throws IOException {
		Injector injector = MainHelper.createInjector();

		LibraryMain main = injector.getInstance(LibraryMain.class);

		main.initializeInst();

	}

	/**
	 * The main method as instance method, which can use dependency injection
	 */
	private void initializeInst() throws IOException {
		Instantiator.instance = instantiator;

		runtimeMonitor.rootCategory.enter();
		runtimeMonitor.startupCategory.enter();

		// create configurations
		Configuration defaultConfiguration = new Configuration();
		Configuration userConfiguration = new Configuration();

		// wire configurations
		configuration.setParentConfiguration(userConfiguration);
		userConfiguration.setParentConfiguration(defaultConfiguration);

		mainHelper.loadDefaultConfiguration(defaultConfiguration);

		mainHelper.loadUserConfiguration(userConfiguration);

		mainHelper.configureLog4j();

		runtimeMonitor.startupCategory.leave();

		// initialize the system information
		systemInfoService.InitializeSystemInformation();
	}

	/**
	 * prints the runtime summary of the measurement driver to the standard
	 * output
	 */
	public void printRuntime() {
		runtimeMonitor.rootCategory.leave();
		runtimeMonitor.print();
	}
}
