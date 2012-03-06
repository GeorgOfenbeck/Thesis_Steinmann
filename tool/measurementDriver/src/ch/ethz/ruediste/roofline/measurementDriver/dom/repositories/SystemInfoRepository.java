package ch.ethz.ruediste.roofline.measurementDriver.dom.repositories;

import java.math.BigInteger;
import java.util.*;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.dom.services.SystemInfoService;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.PmuDescription;

/**
 * Repository for caching information about the system. Should only be used by
 * {@link SystemInfoService}
 */
public class SystemInfoRepository {
	private static Logger log = Logger.getLogger(SystemInfoRepository.class);

	private List<PmuDescription> allPmus;
	private Iterable<PmuDescription> presentPmus;
	private List<Integer> possibleCPUs;

	final private HashSet<BigInteger> observedFrequencies = new HashSet<BigInteger>();

	public HashSet<BigInteger> getObservedFrequencies() {
		return observedFrequencies;
	}

	public List<PmuDescription> getAllPmus() {
		return allPmus;
	}

	public void setAllPmus(List<PmuDescription> allPmus) {
		this.allPmus = allPmus;
	}

	public Iterable<PmuDescription> getPresentPmus() {
		return presentPmus;
	}

	public void setPresentPmus(Iterable<PmuDescription> presentPmus) {
		this.presentPmus = presentPmus;
	}

	public List<Integer> getOnlineCPUs() {
		return possibleCPUs;
	}

	public void setOnlineCPUs(List<Integer> possibleCPUs) {
		this.possibleCPUs = possibleCPUs;
	}
}
