package ch.ethz.ruediste.roofline.measurementDriver.services;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.*;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

public class MeasurementService {
	public static final ConfigurationKey<String> measuringCorePathKey = ConfigurationKey
			.Create(String.class, "measurement.corePath",
					"Path to the measuring core", ".");

	@Inject
	public MultiLanguageSerializationService serializationService;

	@Inject
	public XStream xStream;

	@Inject
	public CommandService commandService;

	@Inject
	public Configuration configuration;

	public MeasurementResult measure(MeasurementCommand command)
	{
		try {
			MeasurementDescription measurement = command.getMeasurement();

			System.out.println("Performing Measurements");

			// loading the measuring Core directory
			File measuringCoreDir = new File(
					configuration.get(measuringCorePathKey));

			// check if the core directory exists
			if (!measuringCoreDir.exists()) {
				throw new Error(
						"Could not find the measuring core. The configured file is: "
								+ measuringCoreDir.getAbsolutePath());
			}

			System.out.println("processing the following measurement:");
			xStream.toXML(command.getMeasurement(), System.out);
			System.out.println();

			// write macro definitions
			writeMacroDefinitions(command, measuringCoreDir);

			// write optimization file
			writeOptimizationFile(measurement, measuringCoreDir);

			// write kernel name
			writeKernelName(measurement, measuringCoreDir);

			// create measurement scheme registration
			writeMeasurementSchemeRegistration(measurement, measuringCoreDir);

			// build
			System.out.println("building measuring core");
			commandService.runCommand(measuringCoreDir, "make",
					new String[] { "-j2", "all" }, 0, true);

			// open the build directory
			File buildDir = new File(measuringCoreDir, "build");

			// write command
			serializeCommand(command, buildDir);

			// remove output file
			System.out.println("removing output file");
			File outputFile = new File(buildDir, "output");
			outputFile.delete();

			// run measurement
			System.out.println("running measurement");
			commandService.runCommand(buildDir, "./measuringCore",
					new String[] {});

			// parse measurer output
			System.out.println("parsing measurement output");
			FileInputStream output = new FileInputStream(outputFile);
			MeasurerOutputCollection outputs = (MeasurerOutputCollection) serializationService
					.DeSerialize(output);

			// create result
			MeasurementResult result = new MeasurementResult();
			result.setMeasurement(measurement);
			result.add(outputs);

			return result;
		} catch (IOException e) {
			throw new Error("Error occured during mesurement", e);
		}
	}

	/**
	 * @param measurement
	 * @param measuringCoreDir
	 * @throws FileNotFoundException
	 */
	public void writeMeasurementSchemeRegistration(
			MeasurementDescription measurement, File measuringCoreDir)
			throws FileNotFoundException {
		System.out
				.println("creating MeasurementScheme registration file");
		File measurementSchemeRegistrationFile = new File(
				measuringCoreDir,
				"generated/MeasurementSchemeRegistration.cpp");
		PrintStream measurementSchemeRegistrationStream = new PrintStream(
				new UpdatingFileOutputStream(measurementSchemeRegistrationFile));

		String schemeName = measurement.getScheme().getClass()
				.getSimpleName();
		schemeName = schemeName.substring(0, schemeName.length()
				- "Description".length());
		String kernelName = measurement.getKernel().getClass()
				.getSimpleName();
		kernelName = kernelName.substring(0, kernelName.length()
				- "Description".length());
		String measurerName = measurement.getMeasurer().getClass()
				.getSimpleName();
		measurerName = measurerName.substring(0, measurerName.length()
				- "Description".length());

		measurementSchemeRegistrationStream
				.printf(
						"#include \"measurementSchemes/%s.h\"\n"
								+ "#include \"kernels/%s.h\"\n"
								+ "#include \"measurers/%s.h\"\n"
								+ "#include \"typeRegistry/TypeRegisterer.h\"\n"
								+ "static TypeRegisterer<%s<%s,%s>,%s,%s > dummy;\n",
						schemeName,
						kernelName,
						measurerName,
						schemeName,
						kernelName,
						measurerName,
						kernelName,
						measurerName);
		measurementSchemeRegistrationStream.close();
	}

	/**
	 * @param measurement
	 * @param measuringCoreDir
	 * @throws FileNotFoundException
	 */
	public void writeOptimizationFile(MeasurementDescription measurement,
			File measuringCoreDir) throws FileNotFoundException {
		System.out.println("creating optimization file");
		File optimizationFile = new File(measuringCoreDir,
				"kernelOptimization.mk");
		PrintStream optimizationPrintStream = new PrintStream(
				new UpdatingFileOutputStream(
						optimizationFile));
		optimizationPrintStream.printf("KERNEL_OPTIMIZATION_FLAGS=%s\n",
				measurement.getKernel().getOptimization());
		optimizationPrintStream.close();
	}

	public void writeKernelName(MeasurementDescription measurement,
			File measuringCoreDir) throws FileNotFoundException {
		System.out.println("writing kernel name");
		File optimizationFile = new File(measuringCoreDir,
				"kernelName.mk");
		PrintStream optimizationPrintStream = new PrintStream(
				new UpdatingFileOutputStream(
						optimizationFile));
		String kernelName = measurement.getKernel().getClass().getSimpleName();
		kernelName = kernelName.substring(0, kernelName.length()
				- "KernelDescription".length());
		optimizationPrintStream.printf("KERNEL_NAME=%s\n", kernelName);
		optimizationPrintStream.close();
	}

	/**
	 * writes the macro definitions
	 */
	public void writeMacroDefinitions(MeasurementCommand command,
			File measuringCoreDir) throws FileNotFoundException {

		System.out.println("Writing macro definitions");

		// create the directories for the macro definition headers
		File macrosDir = new File(measuringCoreDir, "generated/macros");
		macrosDir.mkdirs();

		// load all macro definition keys
		List<Pair<Class<?>, MacroKey>> macros = ClassFinder
				.getStaticFieldValues(MacroKey.class,
						"ch.ethz.ruediste.roofline");

		TreeSet<MacroKey> keySet = new TreeSet<MacroKey>();

		HashSet<File> presentFiles = new HashSet<File>();

		// iterate over all keys and write definition file
		for (Pair<Class<?>, MacroKey> pair : macros) {
			System.out.printf("found macro %s\n", pair.getRight()
					.getMacroName());
			MacroKey macro = pair.getRight();

			// check if macro keys are unique
			if (keySet.contains(macro)) {
				throw new Error("Macro named " + macro.getMacroName()
						+ " defined multiple times");
			}
			keySet.add(macro);

			File outputFile = new File(macrosDir, macro.getMacroName() + ".h");
			presentFiles.add(outputFile);
			PrintStream output = new PrintStream(new UpdatingFileOutputStream(
					outputFile));
			output.printf("// %s\n#define %s %s\n",
					macro.getDescription().replace("\n", "\n// "),
					macro.getMacroName(),
					command.getMeasurement().getMacroDefinition(macro));
			output.close();
		}

		// remove spurious files
		removeFilesNotInSet(macrosDir, presentFiles);
	}

	/**
	 * remove all files in dir which are not in presentFiles
	 */
	private void removeFilesNotInSet(File dir,
			final HashSet<File> presentFiles) {

		// load all files not in presentFiles
		File[] filesToDelete = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return !presentFiles.contains(pathname);
			}
		});

		// delete the files
		for (File file : filesToDelete) {
			file.delete();
		}
	}

	/**
	 * serialized the command
	 */
	public void serializeCommand(MeasurementCommand command,
			File measuringCoreDir) throws IOException, FileNotFoundException {
		File configFile = new File(measuringCoreDir, "config");

		configFile.createNewFile();
		FileOutputStream config = new FileOutputStream(configFile);
		serializationService.Serialize(command, config);
		config.close();
	}

}
