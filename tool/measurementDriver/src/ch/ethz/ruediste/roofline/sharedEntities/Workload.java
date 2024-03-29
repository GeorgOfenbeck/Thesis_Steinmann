package ch.ethz.ruediste.roofline.sharedEntities;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.kernelAxis;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;

public class Workload extends WorkloadData {

	public Workload() {
		setCpu(-1);
		setMeasurerSet(new MeasurerSet());
	}

	public Collection<? extends String> getMacroDefinitions(MacroKey key) {
		ArrayList<String> result = new ArrayList<String>();

		// get macros defined on the workload
		if (isMacroDefined(key)) {
			result.add(getMacroDefinition(key));
		}

		// get macros defined on the kernel
		if (getKernel() != null && getKernel().isMacroDefined(key)) {
			result.add(getKernel().getMacroDefinition(key));
		}

		// get macros defined on the measurer set
		if (getMeasurerSet() != null) {
			result.addAll(getMeasurerSet().getMacroDefinitions(key));
		}

		return result;
	}

	public void initialize(Coordinate coordinate) {
		if (coordinate.contains(kernelAxis))
			setKernel(coordinate.get(kernelAxis));

		if (getKernel() != null)
			getKernel().initialize(coordinate);

		if (getMeasurerSet() != null) {
			getMeasurerSet().initialize(coordinate);
		}
	}

	@Override
	public String toString() {
		ArrayList<String> str = new ArrayList<String>();
		if (getMeasurerSet() != null) {

			for (MeasurerBase mdb : getMeasurerSet().getMeasurers()) {
				str.add(toString(mdb));
			}
		}
		return toString(getKernel()) + "," + StringUtils.join(str, ",");
	}

	private String toString(Object o) {
		if (o == null) {
			return "null";
		}

		String result = o.getClass().getSimpleName();
		if (result.endsWith("Description")) {
			result = result.substring(0,
					result.length() - "Description".length());
		}
		return result;
	}

}
