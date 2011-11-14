package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("list")
public class MultiLanguageList  extends MultiLangugeFieldBase{

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
