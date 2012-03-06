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
		return IterableUtils.single(events, new IUnaryPredicate<String>() {
			public Boolean apply(String event) {
				// split the event name
				String[] eventParts = event.split("::");

				// check if the PMU specified in the event is present
				return getPresentPMU(eventParts[0]) != null;
			}
		});
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

		// make a raw measurement
		configuration.push();
		configuration.set(MeasurementService.measureRawKey, true);

		MeasurementResult result = measurementService.measure(measurement, 1);

		// restore configuration
		configuration.pop();

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
		// setup measurer
		FileMeasurer measurer = new FileMeasurer();
		measurer.addFile("/sys/devices/system/cpu/online");

		// setup measurement
		Measurement measurement = new Measurement();
		Workload workload = new Workload();
		measurement.addWorkload(workload);
		DummyKernel kernel = new DummyKernel();
		kernel.setOptimization("-O2");

		workload.setKernel(kernel);
		workload.setMeasurerSet(new MeasurerSet(measurer));

		// make a raw measurement
		configuration.push();
		configuration.set(MeasurementService.measureRawKey, true);

		// perform measurement
		MeasurementResult result = measurementService.measure(measurement, 1);

		// restore configuration
		configuration.pop();

		// retrieve output
		FileMeasurerOutput output = single(result.getMeasurerOutputs(measurer));
		String fileContent = single(output.getFileContentList())
				.getStopContent().trim();

		return parseCpuList(fileContent);

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
}
