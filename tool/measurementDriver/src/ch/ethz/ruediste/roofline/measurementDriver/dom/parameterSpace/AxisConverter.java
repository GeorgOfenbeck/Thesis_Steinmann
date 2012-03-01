package ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace;

import com.thoughtworks.xstream.converters.*;
import com.thoughtworks.xstream.io.*;

public class AxisConverter implements Converter {
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type) {
		return type == Axis.class;
	}

	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		Axis<?> axis = (Axis<?>) source;
		writer.addAttribute("uid", axis.getUid().toString());
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		String uid = reader.getAttribute("uid");
		Axis<?> axis = AxisReflectionHelper.getAxis(uid);
		if (axis == null) {
			throw new Error("could not find axis with uid " + uid);
		}
		return axis;
	}

}
