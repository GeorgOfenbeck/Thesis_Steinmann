package ch.ethz.ruediste.roofline.measurementDriver.dom.services;

import java.io.File;
import java.util.*;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.configurators.*;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.PerfEventMeasurer;

import com.google.inject.Inject;

/**
 * Service for running measurements. The main functions are
 * 
 * <ul>
 * <li>prepareMeasuringCoreBuilding()()</li>
 * <li>compilePreparedMeasuringCore()</li>
 * <li>runMeasuringCore()</li>
 * </ul>
 */
public class MeasurementService {
	public static final ConfigurationKey<String> systemConfiguratorExecutableKey = ConfigurationKey
			.Create(String.class,
					"systemConfigurator.executable",
					"Path and name of the executable of the system measurementBuilder",
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
	public Configuration configuration;

	@Inject
	public IMeasurementFacilility measurementFacilility;

	@Inject
	public SystemInfoService systemInfoService;

	/**
	 * Run the measurement the specified number of times.
	 * 
	 * Passes the request to the MeasurementAppController
	 */
	public MeasurementResult measure(Measurement measurement, int numberOfRuns) {
		return measurementFacilility.measure(measurement, numberOfRuns);
	}

	public void prepareMeasurement(Measurement measurement) {
		measurement.setIds();
		// add the measurementBuilder setting the frequency scaling if desired
		if (!configuration.get(measureRawKey)
				&& configuration.get(measurementFrequencyGovernorKey) != null) {
			addMeasurementFrequencyConfigurator(measurement);
		}
	}

	/**
	 * adds a configurator to the measurement which sets the CPU frequency
	 * during the measurement. configured by measurementFrequencyGovernorKey and
	 * defaultFrequencyGovernorKey
	 */
	public void addMeasurementFrequencyConfigurator(
			Measurement measurementDescription) {
		// create configurator
		RunCommandConfigurator configurator = new RunCommandConfigurator();
		measurementDescription.getConfigurators().add(configurator);

		// setup run command to set the frequency for the measurement
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

			// set the frequency of all online CPUs
			for (int cpu : systemInfoService.getOnlineCPUs()) {
				cmd.getArgs().add(Integer.toString(cpu));
			}

		}

		// setup run command to set the frequency after the measurement
		{
			RunCommand cmd = new RunCommand();
			configurator.getAfterMeasurementCommands().add(cmd);

			String executable = configuration
					.get(systemConfiguratorExecutableKey);
			cmd.setExecutable(executable);
			cmd.getArgs().add(new File(executable).getName());
			cmd.getArgs().add("governor");
			cmd.getArgs().add(configuration.get(defaultFrequencyGovernorKey));

			// set the frequency of all online CPUs
			for (int cpu : systemInfoService.getOnlineCPUs()) {
				cmd.getArgs().add(Integer.toString(cpu));
			}

		}
	}

	/**
	 * Group the supplied measurers into groups which can be measured at the
	 * same time
	 * 
	 * @param measurers
	 *            to be grouped
	 */
	public List<MeasurerSet> buildSets(List<MeasurerBase> measurers) {
		ArrayList<MeasurerSet> result = new ArrayList<MeasurerSet>();

		// the first set contains all non-perf event measurers
		// each set contains two perf event measurers
		ArrayList<MeasurerBase> nonPerfEventMeasurers = new ArrayList<MeasurerBase>();
		ArrayList<MeasurerBase> perfEventMeasurers = new ArrayList<MeasurerBase>();
		for (MeasurerBase measurer : measurers) {
			if (measurer instanceof PerfEventMeasurer)
				perfEventMeasurers.add(measurer);
			else
				nonPerfEventMeasurers.add(measurer);
		}

		// build the first set
		MeasurerSet set = new MeasurerSet();
		set.getAdditionalMeasurers().addAll(nonPerfEventMeasurers);

		// add the perf event measurers to the measurer sets
		int perfEventMeasurerCount = 0;
		for (MeasurerBase measurer : perfEventMeasurers) {
			// add the measurer to the current measurer set
			set.getAdditionalMeasurers().add(measurer);
			perfEventMeasurerCount++;

			// check if there are enough perf event measurers in the set already
			if (perfEventMeasurerCount == 2) {
				// switch to a new measurer set
				result.add(set);
				set = new MeasurerSet();
				perfEventMeasurerCount = 0;
			}
		}

		// add the last set if necessary
		if (set.getAdditionalMeasurers().size() > 0)
			result.add(set);

		return result;
	}
}
