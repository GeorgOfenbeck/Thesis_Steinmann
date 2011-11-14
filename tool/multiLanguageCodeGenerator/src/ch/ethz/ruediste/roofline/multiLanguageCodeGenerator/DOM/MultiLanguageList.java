package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("list")
public class MultiLanguageList  extends MultiLangugeFieldBase{
	@Override
	public FieldKind getFieldKind() {
		return FieldKind.List;
	}
	
	public String getcType(){
		return String.format("std::vector<%s>",getcItemType());
	}
	
	public String getJavaType(){
		return String.format("List<%s>",getJavaItemType());
	}
	
	public String getJavaItemType(){
		return getTypeDescriptor().getJavaBoxedName();
	}
}
