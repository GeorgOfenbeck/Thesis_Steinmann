package ch.ethz.ruediste.roofline.entities;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.*;

public class ValidationData {

	public static class CpuSpecificFile {
		private final String pattern;
		private final int cpu;

		public CpuSpecificFile(String pattern, int cpu) {
			this.pattern = pattern;
			this.cpu = cpu;
		}

		public String getFileName() {
			return String.format(pattern, cpu);
		}

		public String getPattern() {
			return pattern;
		}

		public int getCpu() {
			return cpu;
		}
	}

	private PerfEventMeasurer perfEventMeasurer;
	private FileMeasurer fileMeasurer;

	final private ArrayList<CpuSpecificFile> thermalThrottleCountFiles = new ArrayList<CpuSpecificFile>();
	final private ArrayList<CpuSpecificFile> currentFrequencyFiles = new ArrayList<CpuSpecificFile>();
	final private ArrayList<CpuSpecificFile> totalStateTransistionsFiles = new ArrayList<CpuSpecificFile>();
	private Configuration validationConfiguration;

	public void setPerfEventMeasurer(PerfEventMeasurer perfEventMeasurer) {
		this.perfEventMeasurer = perfEventMeasurer;

	}

	public PerfEventMeasurer getPerfEventMeasurer() {
		return perfEventMeasurer;
	}

	public void setFileMeasurer(FileMeasurer fileMeasurer) {
		this.fileMeasurer = fileMeasurer;

	}

	public FileMeasurer getFileMeasurer() {
		return fileMeasurer;
	}

	public ArrayList<CpuSpecificFile> getThermalThrottleCountFiles() {
		return thermalThrottleCountFiles;
	}

	public void addThermalThrottleCountFile(
			CpuSpecificFile thermalThrottleCountFile) {
		thermalThrottleCountFiles.add(thermalThrottleCountFile);
	}

	public ArrayList<CpuSpecificFile> getCurrentFrequencyFiles() {
		return currentFrequencyFiles;
	}

	public void addCurrentFrequencyFile(CpuSpecificFile currentFrequencyFile) {
		currentFrequencyFiles.add(currentFrequencyFile);
	}

	public ArrayList<CpuSpecificFile> getTotalStateTransistionsFiles() {
		return totalStateTransistionsFiles;
	}

	public void addTotalStateTransistionsFile(
			CpuSpecificFile getTotalStateTransistionsFile) {
		totalStateTransistionsFiles.add(getTotalStateTransistionsFile);
	}

	public void setValidationConfiguration(Configuration validationConfiguration) {
		this.validationConfiguration = validationConfiguration;
		// TODO Auto-generated method stub

	}

	public Configuration getConfiguration() {
		return validationConfiguration;
	}

}
