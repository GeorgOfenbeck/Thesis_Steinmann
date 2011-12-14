package ch.ethz.ruediste.roofline.measurementDriver.dom;

import ch.ethz.ruediste.roofline.statistics.IAddValue;

public abstract class Plot implements IAddValue {

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