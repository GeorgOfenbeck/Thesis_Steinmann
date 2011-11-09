package ch.ethz.ruediste.roofline.sharing.DOM;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("field")
public class SharedField  extends SharedFieldBase
{
	@Override
	public FieldType getFieldType() {
		return FieldType.Field;
	}
}
