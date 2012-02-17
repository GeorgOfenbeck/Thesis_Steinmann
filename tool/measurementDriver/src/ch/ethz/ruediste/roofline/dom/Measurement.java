package ch.ethz.ruediste.roofline.dom;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.single;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryPredicate;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class Measurement extends MeasurementData {

	@XStreamOmitField
	private ValidationData validationData;

	public Measurement() {
	}

	public Measurement(Coordinate coordinate) {
		initialize(coordinate);
	}

	@Override
	public String toString() {

		return StringUtils.join(getWorkloads(), ":");
	}

	public void initialize(Coordinate coordinate) {
		for (Workload workload : getWorkloads()) {
			workload.initialize(coordinate);
		}

	}

	/**
	 * Return the definition of the macro identified by key. If the macro was
	 * not defined in this measurement description, return the default
	 * definition. If contradicting definitions are found, raise an error
	 */
	public String getMacroDefinition(MacroKey key) {
		List<String> availableDefinitions = new ArrayList<String>();

		// add eventual definition of this
		if (super.isMacroDefined(key)) {
			availableDefinitions.add(super.getMacroDefinition(key));
		}

		// get the macro definitions of all workloads
		for (Workload workload : getWorkloads()) {
			availableDefinitions.addAll(workload.getMacroDefinitions(key));
		}

		// return the default value if no definition has been found
		if (availableDefinitions.size() == 0) {
			return key.getDefaultValue();
		}

		// at least one definition was given. Extract the first one
		String firstDefinition = availableDefinitions.get(0);

		// check if all definitions are equal to the first definition
		for (String definition : availableDefinitions) {
			if (!firstDefinition.equals(definition)) {
				throw new Error(
						String.format(
								"found multiple definitions for macro %s: <%s> and <%s>",
								key.getMacroName(), firstDefinition, definition));
			}
		}

		// since all definitions are equal, it does not matter which one is
		// returned
		return firstDefinition;
	}

	public ValidationData getValidationData() {
		return validationData;
	}

	public void setValidationData(ValidationData validationData) {
		this.validationData = validationData;
	}

	public void addWorkload(Workload workload) {
		getWorkloads().add(workload);
	}

	public void addRule(Rule rule) {
		getRules().add(rule);
	}

	public Iterable<KernelBase> getKernels() {
		ArrayList<KernelBase> result = new ArrayList<KernelBase>();
		for (Workload workload : getWorkloads()) {
			if (workload.getKernel() != null) {
				result.add(workload.getKernel());
			}
		}
		for (ActionBase action : getActions()) {
			result.addAll(action.getKernels());
		}
		return result;
	}

	public void setIds() {
		int workloadId = 0;
		for (Workload workload : getWorkloads()) {
			workload.setId(workloadId++);
		}

		int kernelId = 0;
		for (KernelBase kernel : getKernels()) {
			kernel.setId(kernelId++);
		}

		int measurerSetId = 0;
		for (MeasurerSet measurerSet : getMeasurerSets()) {
			measurerSet.setId(measurerSetId++);
		}

		int measurerId = 0;
		for (MeasurerBase measurer : getMeasurers()) {
			measurer.setId(measurerId++);
		}

	}

	public Iterable<MeasurerSet> getMeasurerSets() {
		ArrayList<MeasurerSet> result = new ArrayList<MeasurerSet>();
		for (Workload workload : getWorkloads()) {
			if (workload.getMeasurerSet() != null) {
				result.add(workload.getMeasurerSet());
			}
		}
		for (ActionBase action : getActions()) {
			result.addAll(action.getMeasurerSets());
		}

		if (getOverallMeasurerSet() != null) {
			result.add(getOverallMeasurerSet());
		}
		return result;
	}

	public Iterable<MeasurerBase> getMeasurers() {
		ArrayList<MeasurerBase> result = new ArrayList<MeasurerBase>();
		for (Workload workload : getWorkloads()) {
			if (workload.getMeasurerSet() != null) {
				result.addAll(workload.getMeasurerSet().getMeasurers());
			}
		}
		for (ActionBase action : getActions()) {
			result.addAll(action.getMeasurers());
		}
		if (getOverallMeasurerSet() != null) {
			result.addAll(getOverallMeasurerSet().getMeasurers());
		}
		return result;
	}

	public Iterable<ActionBase> getActions() {
		ArrayList<ActionBase> result = new ArrayList<ActionBase>();

		for (Rule rule : getRules()) {
			if (rule.getAction() != null) {
				result.add(rule.getAction());
			}
		}
		return result;
	}

	public MeasurerBase getMeasurer(final int measurerId) {
		return single(getMeasurers(), new IUnaryPredicate<MeasurerBase>() {
			public Boolean apply(MeasurerBase arg) {
				return arg.getId() == measurerId;
			}
		});
	}

	public MeasurerSet getMeasurerSet(final int measurerSetId) {
		return single(getMeasurerSets(), new IUnaryPredicate<MeasurerSet>() {
			public Boolean apply(MeasurerSet arg) {
				return arg.getId() == measurerSetId;
			}
		});
	}

}
