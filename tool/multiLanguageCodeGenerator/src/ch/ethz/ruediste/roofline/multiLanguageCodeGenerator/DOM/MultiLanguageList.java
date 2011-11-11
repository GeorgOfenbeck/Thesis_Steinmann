package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("list")
public class MultiLanguageList  extends MultiLangugeFieldBase{
	@Override
	public FieldKind getFieldKind() {
		return FieldKind.List;
	}
}
