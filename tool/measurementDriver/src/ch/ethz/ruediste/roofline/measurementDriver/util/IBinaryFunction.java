package ch.ethz.ruediste.roofline.measurementDriver.util;

public interface IBinaryFunction<T1, T2, R> {
	public R apply(T1 arg1, T2 arg2);
}
