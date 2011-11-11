package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import java.util.HashMap;
import java.util.LinkedList;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;


@XStreamConverter(value=ToAttributedValueConverter.class,strings={"comment"})
public abstract class MultiLangugeFieldBase {
	@XStreamAsAttribute
	private String name;
	@XStreamAsAttribute
	private String type;
	
	@XStreamAsAttribute
	private String comment;
	
	@XStreamOmitField
	private HashMap<String,TypeDescriptor> typeDescriptors;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	
	public abstract FieldKind getFieldKind();
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment=comment;
	}
	
	public void setTypeDescriptors(HashMap<String,TypeDescriptor> typeDescriptors) {
		this.typeDescriptors = typeDescriptors;
		
	}
	
	public TypeDescriptor getTypeDescriptor(){
		if (!typeDescriptors.containsKey(type)){
			throw new Error("type "+type+" not known");
		}
		return typeDescriptors.get(type);
	}
	
	public String getNameUpperCamel(){
		return getName().substring(0,1).toUpperCase()+getName().substring(1);
	}
}
