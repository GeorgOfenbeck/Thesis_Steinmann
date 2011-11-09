package ch.ethz.ruediste.roofline.sharing.DOM;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("list")
public class SharedList  extends SharedFieldBase{
	@Override
	public FieldType getFieldType() {
		return FieldType.List;
	}
}
