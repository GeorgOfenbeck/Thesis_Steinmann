package ch.ethz.ruediste.roofline.dom;

public class FileMeasurerDescription extends FileMeasurerDescriptionData
		implements IMeasurerDescription<FileMeasurerOutput> {

	public void addFile(String format, Object... args) {
		getFilesToRecord().add(String.format(format, args));
	}

}
