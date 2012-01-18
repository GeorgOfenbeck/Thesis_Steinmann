package ch.ethz.ruediste.roofline.measurementDriver.util;

public class Predicates {
	public static INullaryPredicate constant(final boolean cnst) {
		return new INullaryPredicate() {

			public Boolean apply() {
				return cnst;
			}
		};
	}

	public static INullaryPredicate True = constant(true);
	public static INullaryPredicate False = constant(false);
}
