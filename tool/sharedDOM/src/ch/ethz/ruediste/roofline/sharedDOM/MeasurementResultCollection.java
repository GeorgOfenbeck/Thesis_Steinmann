package ch.ethz.ruediste.roofline.sharedDOM;

import java.util.ArrayList;
import java.util.List;

public class MeasurementResultCollection {
	private List<MeasurementResult> results
		=new ArrayList<MeasurementResult>();
	
	public List<MeasurementResult> getResults(){
		return results;
	}

	public void add(MeasurementResult result) {
		results.add(result);
	}
}
