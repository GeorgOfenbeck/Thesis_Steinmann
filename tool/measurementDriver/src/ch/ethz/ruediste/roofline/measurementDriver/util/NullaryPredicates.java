package ch.ethz.ruediste.roofline.measurementDriver.util;

public class NullaryPredicates {
	public static INullaryPredicate constant(final boolean cnst) {
		return new INullaryPredicate() {

			public Boolean apply() {
				return cnst;
			}
		};
	}

	public static INullaryPredicate True() {
		return constant(true);
	}

	public static INullaryPredicate False() {
		return constant(false);
	}
}
