package ch.ethz.ruediste.roofline.sharedEntities.measurers;

import ch.ethz.ruediste.roofline.measurementDriver.util.*;

public class FileMeasurerOutput extends FileMeasurerOutputData {

	public FileContent getContent(final String fileName) {
		return IterableUtils.single(getFileContentList(),
				new IUnaryPredicate<FileContent>() {

					public Boolean apply(FileContent arg) {
						return arg.getFileName().equals(fileName);
					}
				});

	}
}
