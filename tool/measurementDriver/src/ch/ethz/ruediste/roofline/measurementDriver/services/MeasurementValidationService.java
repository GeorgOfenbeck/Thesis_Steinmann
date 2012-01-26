package ch.ethz.ruediste.roofline.measurementDriver.services;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.any;

import java.math.BigInteger;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.ValidationData.CpuSpecificFile;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.SystemInfoRepository;

import com.google.inject.Inject;

public class MeasurementValidationService {
	private static Logger log = Logger
			.getLogger(MeasurementValidationService.class);

	public final static ConfigurationKey<Boolean> validationKey = ConfigurationKey
			.Create(Boolean.class, "validation",
					"if true, perform validation", true);

	public final static ConfigurationKey<Boolean> validateFrequencyKey = ConfigurationKey
			.Create(Boolean.class,
					"validation.frequency",
					"check that the frequency of all measurement runs within one measurement are equal",
					true);

	public final static ConfigurationKey<Boolean> validateOverallFrequencyKey = ConfigurationKey
			.Create(Boolean.class,
					"validation.overallFrequency",
					"check that the frequency of all measurement runs of all measurements are equal",
					true);

	public final static ConfigurationKey<Boolean> validateFrequencyTransitionsKey = ConfigurationKey
			.Create(Boolean.class,
					"validation.frequencyTransistions",
					"check that no frequency transitions occur during measurements",
					true);

	public final static ConfigurationKey<Boolean> validateContextSwitchesKey = ConfigurationKey
			.Create(Boolean.class,
					"validation.contextSwitches",
					"check that there are none or many context switches during the measurement",
					true);

	public final static ConfigurationKey<Boolean> validateThermalThrottlingKey = ConfigurationKey
			.Create(Boolean.class,
					"validation.thermalThrottling",
					"check that no thermal throttlings occur during the measurements",
					true);

	public final static ConfigurationKey<Boolean> validateCpuMigrationsKey = ConfigurationKey
			.Create(Boolean.class,
					"validation.cpuMigrations",
					"check that no cpu migrations occur during the measurements",
					true);

	@Inject
	Configuration configuration;

	@Inject
	SystemInfoRepository systemInfoRepository;

	@SuppressWarnings("unchecked")
	public void addValidationMeasurers(MeasurementDescription measurement) {
		// skip validation if disabled
		if (!configuration.get(validationKey)) {
			return;
		}

		// get measured CPUs
		LinkedList<Integer> measuredCpus = new LinkedList<Integer>();
		{
			if (!(measurement.getScheme() instanceof SimpleMeasurementSchemeDescription)) {
				throw new Error(
						"Validation not supported for measurement scheme "
								+ measurement.getScheme().getClass()
										.getSimpleName());
			}
			SimpleMeasurementSchemeDescription scheme = (SimpleMeasurementSchemeDescription) measurement
					.getScheme();

			measuredCpus.add(scheme.getCpu());
		}

		ValidationData validationData = new ValidationData();
		measurement.setValidationData(validationData);

		// get validation configuration
		Configuration validationConfiguration = new Configuration();
		validationData.setValidationConfiguration(validationConfiguration);
		for (ConfigurationKeyBase key : configuration.getKeySet()) {
			if (key.getKey().startsWith("validation")) {
				validationConfiguration.setUntyped(key,
						configuration.getUntyped(key));
			}
		}

		// create the file measurer
		FileMeasurerDescription fileMeasurer = new FileMeasurerDescription();
		String thermalThrottleCountPattern = "/sys/devices/system/cpu/cpu%d/thermal_throttle/core_throttle_count";
		String currentFrequencyPattern = "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_cur_freq";
		String totalStateTransistionsFile = "/sys/devices/system/cpu/cpu%d/cpufreq/stats/total_trans";
		for (int cpu : measuredCpus) {
			if (validationConfiguration.get(validateThermalThrottlingKey))
			{
				CpuSpecificFile file = new CpuSpecificFile(
						thermalThrottleCountPattern, cpu);
				fileMeasurer.addFile(file.getFileName());
				validationData.addThermalThrottleCountFile(
						file);
			}
			if (any(validationConfiguration.get(validateFrequencyKey,
					validateOverallFrequencyKey)))
			{
				CpuSpecificFile file = new CpuSpecificFile(
						currentFrequencyPattern, cpu);
				fileMeasurer.addFile(file.getFileName());
				validationData.addCurrentFrequencyFile(file);
			}
			if (validationConfiguration.get(validateFrequencyTransitionsKey))
			{
				CpuSpecificFile file = new CpuSpecificFile(
						totalStateTransistionsFile, cpu);
				fileMeasurer.addFile(file.getFileName());
				validationData.addTotalStateTransistionsFile(
						file);
			}
		}

		// is there any file to record?
		if (!fileMeasurer.getFilesToRecord().isEmpty()) {
			measurement.addValidationMeasurer(fileMeasurer);
			validationData.setFileMeasurer(fileMeasurer);
		}

		// create the perf event measurer
		PerfEventMeasurerDescription perfEventMeasurerDescription = new PerfEventMeasurerDescription();
		if (validationConfiguration.get(validateContextSwitchesKey)) {
			perfEventMeasurerDescription.addEvent("contextSwitches",
					"perf::PERF_COUNT_SW_CONTEXT_SWITCHES");
		}

		if (validationConfiguration.get(validateCpuMigrationsKey)) {
			perfEventMeasurerDescription.addEvent("cpuMigrations",
					"perf::PERF_COUNT_SW_CPU_MIGRATIONS");
		}

		// is there any event to measure
		if (!perfEventMeasurerDescription.getEvents().isEmpty()) {
			measurement.addValidationMeasurer(perfEventMeasurerDescription);
			validationData.setPerfEventMeasurer(
					perfEventMeasurerDescription);
		}
	}

	@SuppressWarnings("unchecked")
	public void validate(MeasurementResult result) {
		ValidationData validationData = result.getMeasurement()
				.getValidationData();
		// is there any validation data?
		if (validationData == null) {
			return;
		}

		// is validation enabled?
		if (!validationData.getConfiguration().get(validationKey)) {
			return;
		}

		// check cpu migrations
		if (validationData.getConfiguration().get(validateCpuMigrationsKey)) {
			if (!validationData.getPerfEventMeasurer().getMin("cpuMigrations",
					result).equals(BigInteger.ZERO)) {
				log.warn("Cpu migration(s) observerd");
			}
		}

		// check thermal throttling
		if (validationData.getConfiguration().get(validateThermalThrottlingKey)) {
			for (FileMeasurerOutput fileMeasurerOutput : result
					.getMeasurerOutputs(validationData.getFileMeasurer())) {
				for (CpuSpecificFile file : validationData
						.getThermalThrottleCountFiles()) {
					FileContent content = fileMeasurerOutput
							.getContent(file.getFileName());
					String startContent = content.getStartContent().trim();
					BigInteger start = new BigInteger(startContent);
					String stopContent = content.getStopContent().trim();
					BigInteger stop = new BigInteger(stopContent);
					if (!start.equals(stop)) {
						log.warn("observed thermal throttlings");
					}
				}
			}
		}
		// check frequency
		if (any(validationData.getConfiguration().get(validateFrequencyKey,
				validateOverallFrequencyKey))) {
			HashSet<BigInteger> observedFrequencies = getObservedFrequencies(result);

			if (validationData.getConfiguration().get(validateFrequencyKey)) {
				if (observedFrequencies.size() > 1) {
					log.warn(String
							.format("Multiple frequencies observed within one measurement. Observed Frequencies: %s",
									StringUtils.join(observedFrequencies, ",")));
				}
			}
			;
			boolean frequencyAdded = systemInfoRepository
					.getObservedFrequencies().addAll(
							observedFrequencies);
			if (frequencyAdded && validationData.getConfiguration().get(
					validateOverallFrequencyKey)) {
				if (systemInfoRepository.getObservedFrequencies().size() > 1) {
					log.warn(String
							.format("Multiple frequencies observed within the whole measurement run. Observed Frequencies: %s",
									StringUtils.join(systemInfoRepository
											.getObservedFrequencies(), ",")));
				}
			}
		}

		// check frequency transitions
		if (validationData.getConfiguration().get(
				validateFrequencyTransitionsKey)) {
			boolean anyFailed = false;
			for (FileMeasurerOutput fileMeasurerOutput : result
					.getMeasurerOutputs(validationData.getFileMeasurer())) {
				for (CpuSpecificFile frequencyFile : validationData
						.getTotalStateTransistionsFiles()) {
					String startContent = fileMeasurerOutput
							.getContent(frequencyFile.getFileName())
							.getStartContent().trim();
					log.debug("Total Transitions: start Content: "
							+ startContent);
					BigInteger startTotalTransitions = new BigInteger(
							startContent);
					log.debug("Total Transitions: parsed: "
							+ startTotalTransitions);

					BigInteger stopTotalTransitions = new BigInteger(
							fileMeasurerOutput
									.getContent(frequencyFile.getFileName())
									.getStopContent().trim());

					if (!startTotalTransitions.equals(stopTotalTransitions)) {
						anyFailed = true;
					}
				}
			}
			if (anyFailed) {
				log.warn("Frequency transitions occured during measurement");
			}
		}

	}

	/**
	 * @param result
	 * @return
	 */
	public HashSet<BigInteger> getObservedFrequencies(MeasurementResult result) {
		ValidationData validationData = result.getMeasurement()
				.getValidationData();
		HashSet<BigInteger> observedFrequencies = new HashSet<BigInteger>();
		for (FileMeasurerOutput fileMeasurerOutput : result
				.getMeasurerOutputs(validationData.getFileMeasurer())) {
			for (CpuSpecificFile frequencyFile : validationData
					.getCurrentFrequencyFiles()) {
				String startContent = fileMeasurerOutput
						.getContent(frequencyFile.getFileName())
						.getStartContent().trim();
				log.debug("frequency: startContent: " + startContent);
				observedFrequencies.add(new BigInteger(
						startContent));
				String stopContent = fileMeasurerOutput
						.getContent(frequencyFile.getFileName())
						.getStopContent().trim();
				log.debug("frequency: stopContent: " + stopContent);

				observedFrequencies.add(new BigInteger(
						stopContent));
			}
		}
		return observedFrequencies;
	}
}
