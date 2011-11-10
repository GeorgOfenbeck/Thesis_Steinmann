package ch.ethz.ruediste.roofline.sharedDOM;

import java.util.LinkedList;
import java.util.List;

public class MeasurementCollection {
	private List<MeasurementDescription> descriptions
		=new LinkedList<MeasurementDescription>();

	public List<MeasurementDescription> getDescriptions() {
		return descriptions;
	}

	/**
	 * creates a new MeasurementDescription using the provided kernel
	 * and measurerDescription and adds it to the descriptions list
	 * @param kernel
	 */
	public void addDescription(
			KernelDescriptionBase kernel) {
		MeasurementDescription description=new MeasurementDescription();
		description.setKernelDescription(kernel);
		descriptions.add(description);
		
	}
}
