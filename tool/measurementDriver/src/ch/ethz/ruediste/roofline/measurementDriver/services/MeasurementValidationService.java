package ch.ethz.ruediste.roofline.measurementDriver.services;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.ValidationData.CpuSpecificFile;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.SystemInfoRepository;

import com.google.inject.Inject;

public class MeasurementValidationService {
	public final static ConfigurationKey<Boolean> validationKey = ConfigurationKey
			.Create(Boolean.class, "validation",
					"if true, perform validation", true);

	@Inject
	Configuration configuration;

	@Inject
	SystemInfoRepository systemInfoRepository;

	public void addValidationMeasurers(MeasurementDescription measurement) {
		// skip validation if disabled
		if (!configuration.get(validationKey)) {
			return;
		}

		// add the perf event measurer
		PerfEventMeasurerDescription perfEventMeasurerDescription = new PerfEventMeasurerDescription();
		perfEventMeasurerDescription.addEvent("contextSwitches",
				"perf::PERF_COUNT_SW_CONTEXT_SWITCHES");

		measurement.addAdditionalMeasurer(perfEventMeasurerDescription);
		measurement.getValidationData().setPerfEventMeasurer(
				perfEventMeasurerDescription);

		// add the file measurer
		FileMeasurerDescription fileMeasurer = new FileMeasurerDescription();
		String thermalThrottleCountPattern = "/sys/devices/system/cpu/cpu%d/thermal_throttle/core_throttle_count";
		String currentFrequencyPattern = "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_cur_freq";
		String totalStateTransistionsFile = "/sys/devices/system/cpu/cpu%d/cpufreq/stats/total_trans";
		for (int cpu : systemInfoRepository.getPossibleCPUs()) {
			{
				CpuSpecificFile file = new CpuSpecificFile(
						thermalThrottleCountPattern, cpu);
				fileMeasurer.addFile(file.getFileName());
				measurement.getValidationData().addThermalThrottleCountFile(
						file);
			}
			{
				CpuSpecificFile file = new CpuSpecificFile(
						currentFrequencyPattern, cpu);
				fileMeasurer.addFile(file.getFileName());
				measurement.getValidationData().addCurrentFrequencyFile(file);
			}
			{
				CpuSpecificFile file = new CpuSpecificFile(
						totalStateTransistionsFile, cpu);
				fileMeasurer.addFile(file.getFileName());
				measurement.getValidationData().addTotalStateTransistionsFile(
						file);
			}
		}

		measurement.addAdditionalMeasurer(fileMeasurer);
		measurement.getValidationData().setFileMeasurer(fileMeasurer);
	}
}
