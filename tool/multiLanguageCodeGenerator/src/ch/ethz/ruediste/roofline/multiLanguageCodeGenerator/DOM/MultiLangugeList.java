package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("list")
public class MultiLangugeList  extends MultiLangugeFieldBase{
	@Override
	public FieldType getFieldType() {
		return FieldType.List;
	}
}
