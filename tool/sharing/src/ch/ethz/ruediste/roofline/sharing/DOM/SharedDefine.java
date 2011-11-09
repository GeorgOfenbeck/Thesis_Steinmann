package ch.ethz.ruediste.roofline.sharing.DOM;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("define")
public class SharedDefine extends SharedFieldBase{

	@Override
	public FieldType getFieldType() {
		return FieldType.Define;
	}

}
