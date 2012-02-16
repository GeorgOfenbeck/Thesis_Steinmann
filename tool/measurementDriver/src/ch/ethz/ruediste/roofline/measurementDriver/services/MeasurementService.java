package ch.ethz.ruediste.roofline.measurementDriver.services;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.addAll;

import java.io.*;
import java.util.*;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.*;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.IMeasurementFacilility;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.*;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

public class MeasurementService implements IMeasurementFacilility {
	public static final ConfigurationKey<String> systemConfiguratorExecutableKey = ConfigurationKey
			.Create(String.class,
					"systemConfigurator.executable",
					"Path and name of the executable of the system configurator",
					".");

	public final static ConfigurationKey<Boolean> measureRawKey = ConfigurationKey
			.Create(Boolean.class,
					"measure.raw",
					"don't add any validation of configuration to the measurement",
					false);

	static private Logger log = Logger.getLogger(MeasurementService.class);

	public final static ConfigurationKey<String> measurementFrequencyGovernorKey = ConfigurationKey
			.Create(String.class, "measurementFrequencyGovernor",
					"governor that should be used during measurements",
					"performance");

	public final static ConfigurationKey<String> defaultFrequencyGovernorKey = ConfigurationKey
			.Create(String.class, "defaultFrequencyGovernor",
					"governor that should be set after measurements",
					"ondemand");

	@Inject
	public MultiLanguageSerializationService serializationService;

	@Inject
	public CommandService commandService;

	@Inject
	public Configuration configuration;

	@Inject
	public MeasuringCoreLocationService measuringCoreLocationService;

	@Inject
	public IMeasurementFacilility measurementFacilility;

	@Inject
	public RuntimeMonitor runtimeMonitor;

	@Inject
	public XStream xStream;

	@Inject
	public SystemInfoRepository systemInfoRepository;

	@Inject
	public ReflectionRepository reflectionRepository;

	/**
	 * run the measuring core. it has to be built already
	 * 
	 */
	public MeasurementResult runMeasuringCore(MeasurementCommand command)
			throws IOException, FileNotFoundException, ExecuteException {
		log.info("running measurement");
		log.debug("running measurement " + xStream.toXML(command));

		runtimeMonitor.runMeasurementCategory.enter();
		File buildDir = measuringCoreLocationService.getBuildDir();
		// write command
		serializeCommand(command, buildDir);

		// remove old output file
		log.trace("removing output file");
		File outputFile = new File(buildDir, "output");
		outputFile.delete();

		// run measurement
		log.trace("running measurement");
		commandService.runCommand(buildDir, measuringCoreLocationService
				.getMeasuringCoreParentExecutable().getAbsolutePath(),
				new String[] {}, 0, true);

		// parse measurer output
		log.trace("parsing measurement output");
		FileInputStream output = new FileInputStream(outputFile);
		MeasurementRunOutputCollection outputs = (MeasurementRunOutputCollection) serializationService
				.DeSerialize(output);

		// create result
		MeasurementResult result = new MeasurementResult();
		result.setMeasurement(command.getMeasurement());
		result.add(outputs);
		runtimeMonitor.runMeasurementCategory.leave();
		return result;
	}

	public void compilePreparedMeasuringCore(Measurement measurement)
			throws FileNotFoundException, ExecuteException, IOException {
		runtimeMonitor.compilationCategory.enter();
		// build
		log.info("building measuring core");
		commandService.runCommand(
				measuringCoreLocationService.getMeasuringCoreDir(), "make",
				new String[] { "-j2", "all" }, 0, false);
		runtimeMonitor.compilationCategory.leave();
	}

	/**
	 * prepares the measuring core for the building to perform the specified
	 * measurement. Returns true if anything changed.
	 */
	public boolean prepareMeasuringCoreBuilding(Measurement measurement)
			throws Error, FileNotFoundException {
		runtimeMonitor.buildPreparationCategory.enter();
		File measuringCoreDir = measuringCoreLocationService
				.getMeasuringCoreDir();

		boolean anythingChanged = false;

		// write macro definitions
		anythingChanged |= writeMacroDefinitions(measurement, measuringCoreDir);

		// write optimization file
		anythingChanged |= writeOptimizationFiles(measurement, measuringCoreDir);

		// write kernel name
		anythingChanged |= writeKernelNames(measurement, measuringCoreDir);

		// write present kernels
		anythingChanged |= writePresentKernels(measurement, measuringCoreDir);

		// write additional flags
		anythingChanged |= writeAdditionalBuildFlags(measurement,
				measuringCoreDir);

		// create measurement scheme registration
		anythingChanged |= writeMeasurementSchemeRegistration(measurement,
				measuringCoreDir);
		runtimeMonitor.buildPreparationCategory.leave();
		return anythingChanged;
	}

	/**
	 * @param measurement
	 * @param measuringCoreDir
	 * @throws FileNotFoundException
	 */
	private boolean writeMeasurementSchemeRegistration(Measurement measurement,
			File measuringCoreDir) throws FileNotFoundException {
		/*log.trace("creating MeasurementScheme registration file");
		File measurementSchemeRegistrationFile = new File(measuringCoreDir,
				"generated/MeasurementSchemeRegistration.cpp");
		UpdatingFileOutputStream updatingStream = new UpdatingFileOutputStream(
				measurementSchemeRegistrationFile);
		PrintStream measurementSchemeRegistrationStream = new PrintStream(
				updatingStream);

		String schemeName = measurement.getScheme().getClass().getSimpleName();
		schemeName = schemeName.substring(0, schemeName.length()
				- "Description".length());
		String kernelName = measurement.getKernel().getClass().getSimpleName();
		kernelName = kernelName.substring(0, kernelName.length()
				- "Description".length());
		String measurerName = measurement.getMeasurer().getClass()
				.getSimpleName();
		measurerName = measurerName.substring(0, measurerName.length()
				- "Description".length());

		measurementSchemeRegistrationStream.printf(
				"#include \"measurementSchemes/%s.h\"\n"
						+ "#include \"kernels/%s.h\"\n"
						+ "#include \"measurers/%s.h\"\n"
						+ "#include \"typeRegistry/TypeRegisterer.h\"\n"
						+ "static TypeRegisterer<%s<%s,%s>,%s,%s > dummy;\n",
				schemeName, kernelName, measurerName, schemeName, kernelName,
				measurerName, kernelName, measurerName);
		measurementSchemeRegistrationStream.close();

		return updatingStream.isWriting();*/
		return false;
	}

	/**
	 * write the optimization file. return true if modified
	 */
	private boolean writeOptimizationFiles(Measurement measurement,
			File measuringCoreDir) throws FileNotFoundException {

		log.trace("creating optimization files");
		boolean modified = false;
		HashSet<File> presentFiles = new HashSet<File>();
		for (KernelBase kernel : measurement.getKernels()) {
			File optimizationFile = new File(measuringCoreDir,
					"generated/kernelOptimization/kernelOptimization"
							+ kernel.getName() + ".mk");
			presentFiles.add(optimizationFile);
			optimizationFile.getParentFile().mkdirs();
			UpdatingFileOutputStream updatingStream = new UpdatingFileOutputStream(
					optimizationFile);
			PrintStream optimizationPrintStream = new PrintStream(
					updatingStream);
			optimizationPrintStream.printf("KERNEL_OPTIMIZATION_FLAGS_%s=%s\n",
					kernel.getName(), kernel.getOptimization());
			optimizationPrintStream.close();
			modified |= updatingStream.isWriting();
		}

		modified |= removeFilesNotInSet(new File(measuringCoreDir,
				"generated/kernelOptimization"), presentFiles);

		return modified;
	}

	/**
	 * configure the kernel name. return true if changed
	 */
	private boolean writeKernelNames(Measurement measurement,
			File measuringCoreDir) throws FileNotFoundException {

		log.trace("writing kernel namse");

		// open the file included by the makefile for writing
		File kernelNameFile = new File(measuringCoreDir,
				"generated/kernelNames.mk");
		kernelNameFile.getParentFile().mkdirs();
		UpdatingFileOutputStream updatingStream = new UpdatingFileOutputStream(
				kernelNameFile);
		PrintStream optimizationPrintStream = new PrintStream(updatingStream);

		// write the prefix
		optimizationPrintStream.printf("KERNEL_NAMES=");

		// collect kernel names
		HashSet<String> kernelNames = new HashSet<String>();
		for (KernelBase kernel : measurement.getKernels()) {
			kernelNames.add(kernel.getName());
		}

		// print the kernel names
		optimizationPrintStream.printf("%s\n",
				StringUtils.join(kernelNames, " "));

		// close the output file
		optimizationPrintStream.close();
		return updatingStream.isWriting();

	}

	private boolean writePresentKernels(Measurement measurement,
			File measuringCoreDir) throws FileNotFoundException {

		log.trace("writing present kernels");

		// open the file included by the makefile for writing
		// open the present kernels file
		File presentKernelsFile = new File(measuringCoreDir,
				"generated/PresentKernels.h");
		presentKernelsFile.getParentFile().mkdirs();
		UpdatingFileOutputStream updatingStream = new UpdatingFileOutputStream(
				presentKernelsFile);
		PrintStream presentKernelsPrintStream = new PrintStream(updatingStream);

		// print a macro for each kernel
		for (KernelBase kernel : measurement.getKernels()) {
			presentKernelsPrintStream.printf("#define RMT_KERNEL_PRESENT_%s\n",
					kernel.getClass().getSimpleName());
		}

		// close the output file
		presentKernelsPrintStream.close();
		return updatingStream.isWriting();
	}

	private boolean writeLoglevel(Measurement measurement, File measuringCoreDir)
			throws FileNotFoundException {

		log.trace("writing loglevel");

		// open the file included by the makefile for writing
		// open the present kernels file
		File loglevelFile = new File(measuringCoreDir, "generated/LogLevel.h");
		loglevelFile.getParentFile().mkdirs();
		UpdatingFileOutputStream updatingStream = new UpdatingFileOutputStream(
				loglevelFile);
		PrintStream loglevelPrintStream = new PrintStream(updatingStream);

		String logLevel = "0";
		if (log.getLevel() == Level.ERROR)
			logLevel = "LOGLEVEL_ERROR";
		if (log.getLevel() == Level.WARN)
			logLevel = "LOGLEVEL_WARNING";
		if (log.getLevel() == Level.INFO)
			logLevel = "LOGLEVEL_INFO";
		if (log.getLevel() == Level.DEBUG)
			logLevel = "LOGLEVEL_DEBUG";
		if (log.getLevel() == Level.TRACE)
			logLevel = "LOGLEVEL_TRACE";

		loglevelPrintStream.printf("#define LOG_LOGLEVEL %s\n", logLevel);

		// close the output file
		loglevelPrintStream.close();
		return updatingStream.isWriting();
	}

	private boolean writeAdditionalBuildFlags(Measurement measurement,
			File measuringCoreDir) throws FileNotFoundException {

		HashSet<String> additionalIncludeDirSet = new HashSet<String>();
		for (KernelBase kernel : measurement.getKernels()) {
			addAll(additionalIncludeDirSet, kernel.getAdditionalIncludeDirs()
					.split(" "));
		}
		String additionalIncludeDirs = StringUtils.join(
				additionalIncludeDirSet, " ");

		HashSet<String> additionalLibSet = new HashSet<String>();
		for (KernelBase kernel : measurement.getKernels()) {
			additionalLibSet.add(kernel.getAdditionalLibraries());
		}
		String additionalLibs = StringUtils.join(additionalLibSet, " ");

		// open the file included by the makefile for writing
		File additionalFlagsFile = new File(measuringCoreDir,
				"generated/additionalFlags.mk");
		additionalFlagsFile.getParentFile().mkdirs();
		UpdatingFileOutputStream updatingStream = new UpdatingFileOutputStream(
				additionalFlagsFile);
		PrintStream additionalFlagsPrintStream = new PrintStream(updatingStream);

		// print flags
		additionalFlagsPrintStream.printf("LIBS+=%s\n", additionalLibs);
		additionalFlagsPrintStream.printf("INCLUDE_DIRS+=%s\n",
				additionalIncludeDirs);

		// close the output file
		additionalFlagsPrintStream.close();
		return updatingStream.isWriting();
	}

	/**
	 * writes the macro definitions
	 */
	private boolean writeMacroDefinitions(Measurement measurement,
			File measuringCoreDir) throws FileNotFoundException {

		boolean anythingChanged = false;

		log.trace("Writing macro definitions");

		// create the directories for the macro definition headers
		File macrosDir = new File(measuringCoreDir, "generated/macros");
		macrosDir.mkdirs();

		// load all macro definition keys
		List<Pair<Class<?>, MacroKey>> macros = ClassFinder
				.getStaticFieldValues(MacroKey.class,
						"ch.ethz.ruediste.roofline");

		HashSet<File> presentFiles = new HashSet<File>();

		// iterate over all keys and write definition file
		for (MacroKey macro : reflectionRepository.getMacroKeys()) {
			log.debug(String.format("found macro %s", macro.getMacroName()));

			// open output file
			File outputFile = new File(macrosDir, macro.getMacroName() + ".h");
			presentFiles.add(outputFile);
			UpdatingFileOutputStream updatingStream = new UpdatingFileOutputStream(
					outputFile);
			PrintStream output = new PrintStream(updatingStream);

			// get the definition of the macro
			String macroDefinition = measurement.getMacroDefinition(macro);

			// write normal define
			output.printf("// %s\n#define %s %s\n", macro.getDescription()
					.replace("\n", "\n// "), macro.getMacroName(),
					macroDefinition);

			// check if a combined define can be written
			if (macroDefinition.matches("[a-zA-Z0-9_]*")) {
				output.printf("#define %s__%s\n", macro.getMacroName(),
						macroDefinition);
			}

			output.close();
			anythingChanged |= updatingStream.isWriting();
		}

		// remove spurious files
		anythingChanged |= removeFilesNotInSet(macrosDir, presentFiles);

		return anythingChanged;
	}

	/**
	 * remove all files in dir which are not in presentFiles. Returns true if
	 * any file has been deleted
	 */
	private boolean removeFilesNotInSet(File dir,
			final HashSet<File> presentFiles) {

		// load all files not in presentFiles
		File[] filesToDelete = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return !presentFiles.contains(pathname);
			}
		});

		boolean anythingChanged = false;
		// delete the files
		for (File file : filesToDelete) {
			anythingChanged |= file.delete();
		}
		return anythingChanged;
	}

	/**
	 * serialized the command
	 */
	private void serializeCommand(MeasurementCommand command,
			File measuringCoreDir) throws IOException, FileNotFoundException {
		File configFile = new File(measuringCoreDir, "config");

		configFile.createNewFile();
		FileOutputStream config = new FileOutputStream(configFile);
		serializationService.Serialize(command, config);
		config.close();
	}

	/**
	 * Pass the request to the MeasurementAppController
	 */
	public MeasurementResult measure(Measurement measurement,
			int numberOfMeasurements) {
		return measurementFacilility.measure(measurement, numberOfMeasurements);
	}

	public void prepareMeasurement(Measurement measurement) {
		measurement.setIds();
		// add the configurator setting the frequency scaling if desired
		if (!configuration.get(measureRawKey)
				&& configuration.get(measurementFrequencyGovernorKey) != null) {
			addMeasurementFrequencyConfigurator(measurement);
		}
	}

	/**
	 * @param measurementDescription
	 */
	public void addMeasurementFrequencyConfigurator(
			Measurement measurementDescription) {
		RunCommandConfigurator configurator = new RunCommandConfigurator();
		measurementDescription.getConfigurators().add(configurator);

		{
			RunCommand cmd = new RunCommand();
			configurator.getBeforeMeasurementCommands().add(cmd);

			String executable = configuration
					.get(systemConfiguratorExecutableKey);
			cmd.setExecutable(executable);
			cmd.getArgs().add(new File(executable).getName());
			cmd.getArgs().add("governor");
			cmd.getArgs().add(
					configuration.get(measurementFrequencyGovernorKey));
			for (int cpu : systemInfoRepository.getPossibleCPUs()) {
				cmd.getArgs().add(Integer.toString(cpu));
			}

		}

		{
			RunCommand cmd = new RunCommand();
			configurator.getAfterMeasurementCommands().add(cmd);

			String executable = configuration
					.get(systemConfiguratorExecutableKey);
			cmd.setExecutable(executable);
			cmd.getArgs().add(new File(executable).getName());
			cmd.getArgs().add("governor");
			cmd.getArgs().add(configuration.get(defaultFrequencyGovernorKey));
			for (int cpu : systemInfoRepository.getPossibleCPUs()) {
				cmd.getArgs().add(Integer.toString(cpu));
			}

		}
	}
}
