package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("define")
public class MultiLangugeDefine extends MultiLangugeFieldBase{

	@Override
	public FieldKind getFieldKind() {
		return FieldKind.Define;
	}

}
