package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("field")
public class MultiLangugeField  extends MultiLangugeFieldBase
{
	@Override
	public FieldType getFieldType() {
		return FieldType.Field;
	}
}
