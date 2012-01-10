package ch.ethz.ruediste.roofline.measurementDriver.dom;

/*
 * base class for all plots. A plot object contains all data needed to print a plot
 */
public abstract class Plot {

	private String outputName;
	private String title;

	public Plot() {
		super();
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	public void setOutputName(String format, Object... args) {
		this.outputName = String.format(format, args);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitle(String format, Object... args) {
		this.title = String.format(format, args);
	}

}