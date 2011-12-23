package ch.ethz.ruediste.roofline.measurementDriver.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import ch.ethz.ruediste.roofline.dom.MeasurementCommand;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.MeasurerOutputCollection;
import ch.ethz.ruediste.roofline.dom.MultiLanguageSerializationService;
import ch.ethz.ruediste.roofline.dom.PreprocessorMacro;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.ConfigurationKey;

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

			// write command
			File configFile = new File(measuringCoreDir, "config");
			File configDefFile = new File(measuringCoreDir,
					"../src/configDef.h");
			configFile.createNewFile();
			FileOutputStream config = new FileOutputStream(configFile);
			serializationService.Serialize(command, config);
			config.close();

			// create definitions
			PrintStream configDef = new PrintStream(configDefFile);
			for (PreprocessorMacro macro : command.getMeasurement().getMacros()) {
				configDef.printf("#define %s %s\n", macro.getName(),
						macro.getDefinition());
			}
			configDef.close();

			// create optimization file
			System.out.println("creating optimization file");
			File optimizationFile = new File(measuringCoreDir,
					"../makefile.init");
			PrintStream optimizationPrintStream = new PrintStream(
					optimizationFile);
			optimizationPrintStream.printf("OPTIMIZATION = %s\n",
					measurement.getOptimization());
			optimizationPrintStream.close();

			// create optimization file
			System.out
					.println("creating MeasurementScheme registration file");
			File measurementSchemeRegistrationFile = new File(
					measuringCoreDir,
					"../src/MeasurementSchemeRegistration.cpp");
			PrintStream measurementSchemeRegistrationStream = new PrintStream(
					measurementSchemeRegistrationFile);

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

			// build
			System.out.println("building measuring core");
			commandService.runCommand(measuringCoreDir, "make",
					new String[] { "clean" });
			commandService.runCommand(measuringCoreDir, "make",
					new String[] { "-j2", "all" }, 0, true);

			// remove output file
			System.out.println("removing output file");
			File outputFile = new File(measuringCoreDir, "output");
			outputFile.delete();

			// run measurement
			System.out.println("running measurement");
			commandService.runCommand(measuringCoreDir, "./measuringCore",
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

}
