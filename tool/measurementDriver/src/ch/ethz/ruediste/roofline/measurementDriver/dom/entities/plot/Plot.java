package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

/*
 * base class for all plots. A plot object contains all data needed to print a
 * plot
 */
public abstract class Plot<T extends Plot<?>> {

	private String outputName;
	private String title;

	public Plot() {
		super();
	}

	public String getOutputName() {
		return outputName;
	}

	public T setOutputName(String outputName) {
		this.outputName = outputName;
		return This();
	}

	public T setOutputName(String format, Object... args) {
		this.outputName = String.format(format, args);
		return This();
	}

	public String getTitle() {
		return title;
	}

	public T setTitle(String title) {
		this.title = title;
		return This();
	}

	public T setTitle(String format, Object... args) {
		this.title = String.format(format, args);
		return This();
	}

	@SuppressWarnings("unchecked")
	protected T This() {
		return (T) this;
	}

}