package ch.ethz.ruediste.roofline.measurementDriver.dom.services;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.dom.repositories.SystemInfoRepository;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.DummyKernel;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.*;

import com.google.inject.Inject;

/**
 * Service providing information about the system
 */
public class SystemInfoService {
	private static Logger log = Logger.getLogger(SystemInfoService.class);

	@Inject
	public SystemInfoRepository systemInfoRepository;

	@Inject
	public MeasurementService measurementService;

	@Inject
	public Configuration configuration;

	/**
	 * Take a list of event and return the one available on the system
	 */
	public String getAvailableEvent(String... events) {
		try{
		return IterableUtils.single(events, new IUnaryPredicate<String>() {
			public Boolean apply(String event) {
				// split the event name
				String[] eventParts = event.split("::");

				// check if the PMU specified in the event is present
				return getPresentPMU(eventParts[0]) != null;
			}
		});
		}
		catch (Throwable e){
			throw new Error("None of the following events was available: "+StringUtils.join(events));
		}
	}

	/**
	 * return a list of all PMUs (even PMUs not present on the system)
	 */
	public List<PmuDescription> getAllPmus() {
		if (systemInfoRepository.getAllPmus() == null) {
			systemInfoRepository.setAllPmus(readPMUs(false));
		}
		return systemInfoRepository.getAllPmus();
	}

	/**
	 * Get a PMU description by name
	 */
	public PmuDescription getPMU(final String pmuName) {
		return singleOrDefault(getAllPmus(),
				new IUnaryPredicate<PmuDescription>() {
					public Boolean apply(PmuDescription pmu) {
						return pmu.getPmuName().equals(pmuName);
					}
				});
	}

	/**
	 * get the description of a present PMU by name
	 * 
	 * @param pmuName
	 * @return
	 */
	public PmuDescription getPresentPMU(final String pmuName) {
		return IterableUtils.singleOrDefault(getPresentPmus(),
				new IUnaryPredicate<PmuDescription>() {
					public Boolean apply(PmuDescription pmu) {
						return pmu.getPmuName().equals(pmuName);
					}
				});
	}

	/**
	 * read the PMU descriptions using the measuring core
	 */
	private List<PmuDescription> readPMUs(Boolean onlyPresent) {
		// setup measurer
		ListEventsMeasurer measurer = new ListEventsMeasurer();
		measurer.setOnlyPresent(onlyPresent);

		// setup measurement
		Measurement measurement = new Measurement();
		Workload workload = new Workload();
		measurement.addWorkload(workload);
		DummyKernel kernel = new DummyKernel();
		kernel.setOptimization("-O2");

		workload.setKernel(kernel);
		workload.setMeasurerSet(new MeasurerSet(measurer));

		MeasurementResult result = measureRaw(measurement);

		// get the output of the measurer
		ListEventsMeasurerOutput output = single(result
				.getMeasurerOutputs(measurer));

		return output.getPmus();
	}

	/**
	 * return the list of present PMUs
	 */
	public Iterable<PmuDescription> getPresentPmus() {
		// have the present pmus been accessed already?
		if (systemInfoRepository.getPresentPmus() == null) {
			// are all pmus loaded already?
			if (systemInfoRepository.getAllPmus() != null) {
				// get list of present pmus from list of all pmus
				systemInfoRepository.setPresentPmus(where(
						systemInfoRepository.getAllPmus(),
						new IUnaryPredicate<PmuDescription>() {
							public Boolean apply(PmuDescription pmu) {
								return pmu.getIsPresent();
							}
						}));
			}
			else {
				// only retrieve the present pmus
				systemInfoRepository.setPresentPmus(readPMUs(true));
			}
		}

		// return the list of present pmus
		return systemInfoRepository.getPresentPmus();
	}

	/**
	 * concatenate the names of the present PMUs with commas
	 */
	public String getPresentPmuStringList() {
		ArrayList<String> pmus = new ArrayList<String>();
		for (PmuDescription pmu : getPresentPmus()) {
			pmus.add(pmu.getPmuName());
		}
		return StringUtils.join(pmus, ",");
	}

	/**
	 * get the list of online CPUs
	 */
	public List<Integer> getOnlineCPUs() {
		if (systemInfoRepository.getOnlineCPUs() == null) {
			systemInfoRepository.setOnlineCPUs(readOnlineCPUs());
		}
		return systemInfoRepository.getOnlineCPUs();
	}

	/**
	 * Use the measuring core to read the online CPUs
	 */
	private List<Integer> readOnlineCPUs() {
		String fileName = "/sys/devices/system/cpu/online";

		String fileContent = readFileUsingCore(fileName);

		return parseCpuList(fileContent);

	}

	/**
	 * perform a measurement to read the specified file using the core
	 */
	private String readFileUsingCore(String fileName) {
		// setup measurer
		FileMeasurer measurer = new FileMeasurer();
		measurer.addFile(fileName);

		// setup measurement
		Measurement measurement = new Measurement();
		Workload workload = new Workload();
		measurement.addWorkload(workload);
		DummyKernel kernel = new DummyKernel();
		kernel.setOptimization("-O2");

		workload.setKernel(kernel);
		workload.setMeasurerSet(new MeasurerSet(measurer));

		MeasurementResult result = measureRaw(measurement);

		// retrieve output
		FileMeasurerOutput output = single(result.getMeasurerOutputs(measurer));
		String fileContent = single(output.getFileContentList())
				.getStopContent().trim();
		return fileContent;
	}

	/**
	 * perform a measurement without any additional preprocessing
	 */
	private MeasurementResult measureRaw(Measurement measurement) {
		// make a raw measurement
		configuration.push();
		configuration.set(MeasurementService.measureRawKey, true);

		// perform measurement
		MeasurementResult result = measurementService.measure(measurement, 1);

		// restore configuration
		configuration.pop();
		return result;
	}

	/**
	 * Parse a list of CPU numbers. The entries are separated by commas, with
	 * optional ranges. Example: 1,3,4-7,10 results in 1,3,4,5,6,7,10
	 */
	private List<Integer> parseCpuList(String list) {
		List<Integer> result = new ArrayList<Integer>();
		log.debug("parsing CPU list " + list);
		String[] parts = list.split(",");

		for (String part : parts) {
			log.debug("parsing CPU list part " + part);
			// is the part a range?
			if (part.contains("-")) {
				String[] startStop = part.split("-");
				log.debug("part was split into <"
						+ StringUtils.join(startStop, "//") + ">");
				int start = Integer.parseInt(startStop[0]);
				int stop = Integer.parseInt(startStop[1]);
				for (int i = start; i <= stop; i++) {
					result.add(i);
				}
			}
			else {
				result.add(Integer.getInteger(part));
			}
		}

		return result;
	}

	public boolean isPMUPresent(String pmuName) {
		return getPresentPMU(pmuName) != null;
	}

	public CpuType getCpuType() {

		if (isPMUPresent("core"))
			return CpuType.Core;

		if (isPMUPresent("coreduo"))
			return CpuType.Yonah;

		if (isPMUPresent("snb"))
			return CpuType.SandyBridge;
		
		if (isPMUPresent("snb_ep"))
			return CpuType.SandyBridgeExtreme;

		throw new Error("CPU not supported, available PMUs: "
				+ getPresentPmuStringList());
	}

	/**
	 * return the Last Level Cache size
	 */
	public long getLLCCacheSize() {
		switch (getCpuType()) {
		case Yonah:
			return 1024L * 1024L * 2L;
		case Core:
			return 1024L * 1024L * 4L;
		case SandyBridge:
			return 1024L * 1024L * 12L;
		case SandyBridgeExtreme:
			return 1024L * 1024L * 12L;
		}

		throw new Error("CPU not supported. CpuType: " + getCpuType());
	}

	/**
	 * Tests if the measurements are performed on a 64Bit system
	 */
	public boolean is64Bit() {
		if (systemInfoRepository.getIs64Bit() == null) {
			systemInfoRepository.setIs64Bit(measureIs64Bit());
		}

		return systemInfoRepository.getIs64Bit();
	}

	/**
	 * measures if the system is a 64Bit system
	 */
	private boolean measureIs64Bit() {
		// setup measurer
		Ia64Measurer measurer = new Ia64Measurer();

		// setup measurement
		Measurement measurement = new Measurement();
		Workload workload = new Workload();
		measurement.addWorkload(workload);
		DummyKernel kernel = new DummyKernel();
		kernel.setOptimization("-O2");

		workload.setKernel(kernel);
		workload.setMeasurerSet(new MeasurerSet(measurer));

		MeasurementResult result = measureRaw(measurement);
		return single(result.getMeasurerOutputs(measurer)).getIsIa64();
	}

	/**
	 * initialize the sytem information stored n the systemInfoRepository
	 */
	public void InitializeSystemInformation() {
		SystemInformation systemInformation = new SystemInformation();
		systemInformation.CpuType = getCpuType();
		systemInformation.Is64Bit = is64Bit();
		systemInformation.LLCCacheSize = getLLCCacheSize();
		systemInfoRepository.setSystemInformation(systemInformation);
	}

	/**
	 * retrieve the system information from the repository.
	 * InitializeSystemInformation() has to be called first (done during system
	 * initialization)
	 */
	public SystemInformation getSystemInformation() {
		return systemInfoRepository.getSystemInformation();
	}

	public boolean isFrequencyScalingAvailable() {
		switch (getCpuType()) {
		case Yonah:
			return true;
		case Core:
			return true;
		case SandyBridge:
		case SandyBridgeExtreme:
			return false;
		}

		throw new Error("CPU not supported. CpuType: " + getCpuType());
	}
}
