package ch.ethz.ruediste.roofline.measurementDriver.dom;

/*
 * A 2D plot, specifies the axis names and units
 */
public class Plot2D extends Plot {
	private String xLabel;
	private String xUnit;
	private String yLabel;
	private String yUnit;

	public String getxLabel() {
		return xLabel;
	}

	public void setxLabel(String xLabel) {
		this.xLabel = xLabel;
	}

	public String getxUnit() {
		return xUnit;
	}

	public void setxUnit(String xUnit) {
		this.xUnit = xUnit;
	}

	public String getyLabel() {
		return yLabel;
	}

	public void setyLabel(String yLabel) {
		this.yLabel = yLabel;
	}

	public String getyUnit() {
		return yUnit;
	}

	public void setyUnit(String yUnit) {
		this.yUnit = yUnit;
	}
}
