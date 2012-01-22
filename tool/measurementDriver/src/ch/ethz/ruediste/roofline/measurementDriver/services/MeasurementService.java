package ch.ethz.ruediste.roofline.measurementDriver.services;

import java.io.*;
import java.util.*;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.IMeasurementFacilility;

import com.google.inject.Inject;

public class MeasurementService implements IMeasurementFacilility {
	static private Logger log = Logger.getLogger(MeasurementService.class);

	@Inject
	public MultiLanguageSerializationService serializationService;

	@Inject
	public CommandService commandService;

	@Inject
	public Configuration configuration;

	@Inject
	MeasuringCoreLocationService measuringCoreLocationService;

	@Inject
	IMeasurementFacilility measurementFacilility;

	/**
	 * run the measuring core. it has to be built already
	 * 
	 */
	public MeasurementResult runMeasuringCore(MeasurementCommand command)
			throws IOException, FileNotFoundException, ExecuteException {

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
				.getMeasuringCoreExecutable().getAbsolutePath(),
				new String[] {}, 0, false);

		// parse measurer output
		log.trace("parsing measurement output");
		FileInputStream output = new FileInputStream(outputFile);
		MeasurerOutputCollection outputs = (MeasurerOutputCollection) serializationService
				.DeSerialize(output);

		// create result
		MeasurementResult result = new MeasurementResult();
		result.setMeasurement(command.getMeasurement());
		result.add(outputs);
		return result;
	}

	public void buildPreparedMeasuringCore(MeasurementDescription measurement)
			throws FileNotFoundException, ExecuteException, IOException {

		// build
		log.trace("building measuring core");
		commandService.runCommand(measuringCoreLocationService
				.getMeasuringCoreDir(), "make", new String[] {
				"-j2", "all" }, 0, false);
	}

	/**
	 * prepares the measuring core for the building to perform the specified
	 * measurement. Returns true if anything changed.
	 */
	public boolean prepareMeasuringCoreBuilding(
			MeasurementDescription measurement)
			throws Error, FileNotFoundException {
		File measuringCoreDir = measuringCoreLocationService
				.getMeasuringCoreDir();

		boolean anythingChanged = false;

		// write macro definitions
		anythingChanged |= writeMacroDefinitions(measurement, measuringCoreDir);

		// write optimization file
		anythingChanged |= writeOptimizationFile(measurement, measuringCoreDir);

		// write kernel name
		anythingChanged |= writeKernelName(measurement, measuringCoreDir);

		// create measurement scheme registration
		anythingChanged |= writeMeasurementSchemeRegistration(measurement,
				measuringCoreDir);

		return anythingChanged;
	}

	/**
	 * @param measurement
	 * @param measuringCoreDir
	 * @throws FileNotFoundException
	 */
	private boolean writeMeasurementSchemeRegistration(
			MeasurementDescription measurement, File measuringCoreDir)
			throws FileNotFoundException {
		log.trace("creating MeasurementScheme registration file");
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

		return updatingStream.isWriting();
	}

	/**
	 * write the optimization file. return true if modified
	 */
	private boolean writeOptimizationFile(MeasurementDescription measurement,
			File measuringCoreDir) throws FileNotFoundException {
		log.trace("creating optimization file");
		File optimizationFile = new File(measuringCoreDir,
				"kernelOptimization.mk");
		UpdatingFileOutputStream updatingStream = new UpdatingFileOutputStream(
				optimizationFile);
		PrintStream optimizationPrintStream = new PrintStream(
				updatingStream);
		optimizationPrintStream.printf("KERNEL_OPTIMIZATION_FLAGS=%s\n",
				measurement.getKernel().getOptimization());
		optimizationPrintStream.close();
		return updatingStream.isWriting();
	}

	/**
	 * configure the kernel name. return true if changed
	 */
	private boolean writeKernelName(MeasurementDescription measurement,
			File measuringCoreDir) throws FileNotFoundException {
		log.trace("writing kernel name");
		File optimizationFile = new File(measuringCoreDir, "kernelName.mk");
		UpdatingFileOutputStream updatingStream = new UpdatingFileOutputStream(
				optimizationFile);
		PrintStream optimizationPrintStream = new PrintStream(
				updatingStream);
		String kernelName = measurement.getKernel().getClass().getSimpleName();
		kernelName = kernelName.substring(0, kernelName.length()
				- "KernelDescription".length());
		optimizationPrintStream.printf("KERNEL_NAME=%s\n", kernelName);
		optimizationPrintStream.close();
		return updatingStream.isWriting();
	}

	/**
	 * writes the macro definitions
	 */
	private boolean writeMacroDefinitions(MeasurementDescription measurement,
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

		TreeSet<MacroKey> keySet = new TreeSet<MacroKey>();

		HashSet<File> presentFiles = new HashSet<File>();

		// iterate over all keys and write definition file
		for (Pair<Class<?>, MacroKey> pair : macros) {
			log.debug(String.format("found macro %s\n", pair.getRight()
					.getMacroName()));
			MacroKey macro = pair.getRight();

			// check if macro keys are unique
			if (keySet.contains(macro)) {
				throw new Error("Macro named " + macro.getMacroName()
						+ " defined multiple times");
			}
			keySet.add(macro);

			File outputFile = new File(macrosDir, macro.getMacroName() + ".h");
			presentFiles.add(outputFile);
			UpdatingFileOutputStream updatingStream = new UpdatingFileOutputStream(
					outputFile);
			PrintStream output = new PrintStream(updatingStream);
			output.printf("// %s\n#define %s %s\n", macro.getDescription()
					.replace("\n", "\n// "), macro.getMacroName(), measurement
					.getMacroDefinition(macro));
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
	public MeasurementResult measure(MeasurementDescription measurement,
			int numberOfMeasurements) {
		return measurementFacilility.measure(measurement, numberOfMeasurements);
	}

}
