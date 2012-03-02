package ch.ethz.ruediste.roofline.measurementDriver.dom.services;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.addAll;

import java.io.*;
import java.util.HashSet;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.repositories.ReflectionRepository;
import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.serializationService.SharedEntitySerializationService;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

public class MeasuringCoreService {
	private final static Logger log = Logger
			.getLogger(MeasuringCoreService.class);

	@Inject
	public SharedEntitySerializationService serializationService;

	@Inject
	public CommandService commandService;

	@Inject
	public MeasuringCoreLocationService measuringCoreLocationService;

	@Inject
	public RuntimeMonitor runtimeMonitor;

	@Inject
	public XStream xStream;

	@Inject
	public ReflectionRepository reflectionRepository;

	/**
	 * runs the measuring core. it has to be built already
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

	/**
	 * compiles the prepared measuring core
	 * 
	 * @param measurement
	 */
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

		// write log level
		anythingChanged |= writeLoglevel(measurement, measuringCoreDir);

		// write additional flags
		anythingChanged |= writeAdditionalBuildFlags(measurement,
				measuringCoreDir);

		runtimeMonitor.buildPreparationCategory.leave();
		return anythingChanged;
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

		String logLevel = "LOGLEVEL_WARNING";
		if (log.isInfoEnabled()) {
			logLevel = "LOGLEVEL_INFO";
		}
		if (log.isDebugEnabled()) {
			logLevel = "LOGLEVEL_DEBUG";
		}
		if (log.isTraceEnabled()) {
			logLevel = "LOGLEVEL_TRACE";
		}

		loglevelPrintStream.printf("#define LOGLEVEL %s\n", logLevel);

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
				output.printf("#define %s__%s 1\n", macro.getMacroName(),
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
	 * serialize the command
	 */
	private void serializeCommand(MeasurementCommand command,
			File measuringCoreDir) throws IOException, FileNotFoundException {
		File configFile = new File(measuringCoreDir, "config");

		configFile.createNewFile();
		FileOutputStream config = new FileOutputStream(configFile);
		serializationService.Serialize(command, config);
		config.close();
	}
}
