package ch.ethz.ruediste.roofline.sharedEntities;

/**
 * An operation to be measured. (As in Operational Intensity)
 */
public enum Operation {
	SinglePrecisionFlop, DoublePrecisionFlop, CompInstr, SSEFlop,
}