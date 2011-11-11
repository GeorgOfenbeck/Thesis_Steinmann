package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;
import java.util.HashMap;
import java.util.LinkedList;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("class")
public class MultiLangugeClass {
	
	@XStreamImplicit
	private LinkedList<MultiLangugeFieldBase> fields
		=new LinkedList<MultiLangugeFieldBase>();
	
	@XStreamAsAttribute
	private String name;
	@XStreamAsAttribute
	private String cBaseType;
	
	@XStreamAsAttribute
	private String javaBaseType;

	@XStreamAsAttribute()
	private String comment;

	public LinkedList<MultiLangugeFieldBase> getFields() {
		return fields;
	}

	public void addField(MultiLangugeFieldBase sharedField) {
		fields.add(sharedField);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getcBaseType() {
		return cBaseType;
	}

	public void setcBaseType(String cBaseType) {
		this.cBaseType = cBaseType;
	}

	public String getJavaBaseType() {
		return javaBaseType;
	}

	public void setJavaBaseType(String javaBaseType) {
		this.javaBaseType = javaBaseType;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment=comment;
	}

	public void setTypeDescriptors(HashMap<String, TypeDescriptor> typeDescriptors) {
		for (MultiLangugeFieldBase field: fields){
			field.setTypeDescriptors(typeDescriptors);
		}
	}

	public String getNameUpperCamel(){
		return getName().substring(0,1).toUpperCase()+getName().substring(1);
	}

}
