package ch.ethz.ruediste.roofline.measurementDriver.util;

public interface IUnaryFunction<T1, R> {
	public R apply(T1 arg);
}
