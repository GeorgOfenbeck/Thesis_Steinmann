package ch.ethz.ruediste.roofline.dom;

public class FileMeasurer extends FileMeasurerData
		implements IMeasurer<FileMeasurerOutput> {

	public void addFile(String format, Object... args) {
		getFilesToRecord().add(String.format(format, args));
	}

	public void validate(FileMeasurerOutput output,
			MeasurementResult measurementResult) {
		// TODO Auto-generated method stub

	}

}
