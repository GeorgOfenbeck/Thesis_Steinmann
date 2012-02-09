package ch.ethz.ruediste.roofline.measurementDriver.repositories;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;

import java.math.BigInteger;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;

import com.google.inject.Inject;

/**
 * Repository giving access to the descriptions of the Performance Measuring
 * Units (PMUs) available on the system
 */
public class SystemInfoRepository {
	private static Logger log = Logger.getLogger(SystemInfoRepository.class);
	@Inject
	public MeasurementService measurementService;

	@Inject
	public Configuration configuration;

	private List<PmuDescription> allPmusImp;
	private Iterable<PmuDescription> presentPmusImp;
	private List<Integer> possibleCPUs;

	final private HashSet<BigInteger> observedFrequencies = new HashSet<BigInteger>();

	public String getAvailableEvent(String... events) {
		return IterableUtils.single(events, new IUnaryPredicate<String>() {

			public Boolean apply(String event) {
				String[] eventParts = event.split("::");
				return getPresentPMU(eventParts[0]) != null;
			}
		});
	}

	public List<PmuDescription> getAllPmus() {
		if (allPmusImp == null) {
			allPmusImp = readPMUs(false);
		}
		return allPmusImp;
	}

	public PmuDescription getPMU(final String pmuName) {
		return singleOrDefault(getAllPmus(),
				new IUnaryPredicate<PmuDescription>() {
					public Boolean apply(PmuDescription pmu) {
						return pmu.getPmuName().equals(pmuName);
					}
				});
	}

	public PmuDescription getPresentPMU(final String pmuName) {
		return IterableUtils.singleOrDefault(getAllPmus(),
				new IUnaryPredicate<PmuDescription>() {
					public Boolean apply(PmuDescription pmu) {
						return pmu.getIsPresent()
								&& pmu.getPmuName().equals(pmuName);
					}
				});
	}

	private List<PmuDescription> readPMUs(Boolean onlyPresent) {
		// list all available performance counters
		ListEventsMeasurerDescription measurer = new ListEventsMeasurerDescription();
		measurer.setOnlyPresent(onlyPresent);

		MeasurementDescription measurement = new MeasurementDescription();
		WorkloadDescription workload = new WorkloadDescription();
		measurement.addWorkload(workload);
		DummyKernelDescription kernel = new DummyKernelDescription();
		kernel.setOptimization("-O2");

		workload.setKernel(kernel);
		workload.setMeasurerSet(new MeasurerSet(measurer));

		// make a raw measurement
		configuration.push();
		configuration.set(MeasurementService.measureRawKey, true);

		MeasurementResult result = measurementService.measure(measurement, 1);

		// restore configuration
		configuration.pop();

		ListEventsMeasurerOutput output = single(result
				.getMeasurerOutputs(measurer));

		return output.getPmus();
	}

	public Iterable<PmuDescription> getPresentPmus() {
		// have the present pmus been accessed already?
		if (presentPmusImp == null) {
			// are all pmus loaded already?
			if (allPmusImp != null) {
				// get list of present pmus from list of all pmus
				presentPmusImp = where(getAllPmus(),
						new IUnaryPredicate<PmuDescription>() {
							public Boolean apply(PmuDescription pmu) {
								return pmu.getIsPresent();
							}
						});
			}
			else {
				// only retrieve the present pmus
				presentPmusImp = readPMUs(true);
			}
		}

		// return the list of present pmus
		return presentPmusImp;
	}

	public List<Integer> getPossibleCPUs() {
		if (possibleCPUs == null) {
			possibleCPUs = readPossibleCPUs();
		}
		return possibleCPUs;
	}

	private List<Integer> readPossibleCPUs() {
		// list all possible cpus
		FileMeasurerDescription measurer = new FileMeasurerDescription();
		measurer.addFile("/sys/devices/system/cpu/possible");

		MeasurementDescription measurement = new MeasurementDescription();
		WorkloadDescription workload = new WorkloadDescription();
		measurement.addWorkload(workload);
		DummyKernelDescription kernel = new DummyKernelDescription();
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

		FileMeasurerOutput output = single(result.getMeasurerOutputs(measurer));
		return parseCpuList(single(output.getFileContentList())
				.getStopContent().trim());

	}

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

	public HashSet<BigInteger> getObservedFrequencies() {
		return observedFrequencies;
	}
}
