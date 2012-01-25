package ch.ethz.ruediste.roofline.dom;

import java.util.ArrayList;

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

	private PerfEventMeasurerDescription perfEventMeasurer;
	private FileMeasurerDescription fileMeasurer;

	final private ArrayList<CpuSpecificFile> thermalThrottleCountFiles = new ArrayList<CpuSpecificFile>();
	final private ArrayList<CpuSpecificFile> currentFrequencyFiles = new ArrayList<CpuSpecificFile>();
	final private ArrayList<CpuSpecificFile> totalStateTransistionsFiles = new ArrayList<CpuSpecificFile>();

	public void setPerfEventMeasurer(
			PerfEventMeasurerDescription perfEventMeasurer) {
		this.perfEventMeasurer = perfEventMeasurer;

	}

	public PerfEventMeasurerDescription getPerfEventMeasurer() {
		return perfEventMeasurer;
	}

	public void setFileMeasurer(FileMeasurerDescription fileMeasurer) {
		this.fileMeasurer = fileMeasurer;

	}

	public FileMeasurerDescription getFileMeasurer() {
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

}
