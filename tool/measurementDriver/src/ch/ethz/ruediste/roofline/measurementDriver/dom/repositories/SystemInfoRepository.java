package ch.ethz.ruediste.roofline.measurementDriver.dom.repositories;

import java.math.BigInteger;
import java.util.*;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.dom.services.SystemInfoService;
import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;
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
	private Boolean is64Bit;

	private SystemInformation systemInformation;

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

	public Boolean getIs64Bit() {
		return is64Bit;
	}

	public void setIs64Bit(Boolean is64Bit) {
		this.is64Bit = is64Bit;
	}

	public SystemInformation getSystemInformation() {
		return systemInformation;
	}

	public void setSystemInformation(SystemInformation systemInformation) {
		this.systemInformation = systemInformation;
	}
}
