package ch.ethz.ruediste.roofline.sharedEntities;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryPredicate;

public class Measurement extends MeasurementData {

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
	@Override
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

	public void addWorkload(Workload workload) {
		getWorkloads().add(workload);
	}

	public void addRule(Rule rule) {
		getRules().add(rule);
	}

	public Iterable<KernelBase> getKernels() {
		return getAll(KernelBase.class);
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
		return getAll(MeasurerSet.class);
	}

	public Iterable<MeasurerBase> getMeasurers() {
		return getAll(MeasurerBase.class);
	}

	public Iterable<ActionBase> getActions() {
		return getAll(ActionBase.class);
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

	public Set<Object> getAll() {
		LinkedHashSet<Object> result = new LinkedHashSet<Object>();
		addAll(result);
		return result;
	}

	public <T> Iterable<T> getAll(Class<T> clazz) {
		return ofType(clazz, getAll());
	}
}
