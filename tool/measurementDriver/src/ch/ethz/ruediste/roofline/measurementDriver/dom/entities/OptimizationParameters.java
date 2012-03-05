package ch.ethz.ruediste.roofline.measurementDriver.dom.entities;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.sharedEntities.KernelBase;

public class OptimizationParameters {
	public enum Comparison {
		lessThan, moreThan
	}

	public KernelBase kernel;
	public ParameterSpace optimizationSpace;
	public Coordinate measurementPoint;
	public Comparison comparison;

	public OptimizationParameters(KernelBase kernel,
			ParameterSpace optimizationSpace, Coordinate measurementPoint,
			Comparison comparison) {
		this.kernel = kernel;
		this.optimizationSpace = optimizationSpace;
		this.measurementPoint = measurementPoint;
		this.comparison = comparison;
	}
}
