package ch.ethz.ruediste.roofline.dom;

public class ListEventsMeasurerDescription extends
		ListEventsMeasurerDescriptionData {

	/**
	 * architecture to be listed. example: PFM_PMU_PERF_EVENT, listed in
	 * pfmlib.h. Passed to libpfm4
	 */
	public static String architectureMacroName = "RMT_PERF_EVENT_ARCHITECTURE";
}
