package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

import ch.ethz.ruediste.roofline.measurementDriver.util.IBinaryPredicate;

public abstract class Quantity implements Comparable<Quantity> {
	public abstract double getValue();

	@Override
	public String toString() {
		return Double.toString(getValue());
	}

	public static IBinaryPredicate<Quantity, Quantity> lessThan = new IBinaryPredicate<Quantity, Quantity>() {

		public Boolean apply(Quantity arg1, Quantity arg2) {
			return arg1.getValue() < arg2.getValue();
		}
	};

	public static IBinaryPredicate<Quantity, Quantity> moreThan = new IBinaryPredicate<Quantity, Quantity>() {

		public Boolean apply(Quantity arg1, Quantity arg2) {
			return arg1.getValue() > arg2.getValue();
		}
	};

	public int compareTo(Quantity o) {
		if (o == null)
			throw new Error("Cannot compare to null");
		if (o.getClass() != this.getClass())
			throw new Error(String.format("Cannot compare %s to %s", getClass()
					.getSimpleName(), o.getClass().getSimpleName()));
		return Double.compare(getValue(), o.getValue());
	};
}
