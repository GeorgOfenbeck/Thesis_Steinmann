package ch.ethz.ruediste.roofline.dom;

import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;

public class ListEventsMeasurerDescription extends
		ListEventsMeasurerDescriptionData {

	public static MacroKey architectureMacro =
			MacroKey.Create(
					"RMT_PERF_EVENT_ARCHITECTURE",
					"architecture to be listed by the list events measurer. " +
							"listed in pfmlib.h. Passed to libpfm4",
					"PFM_PMU_PERF_EVENT");
}
