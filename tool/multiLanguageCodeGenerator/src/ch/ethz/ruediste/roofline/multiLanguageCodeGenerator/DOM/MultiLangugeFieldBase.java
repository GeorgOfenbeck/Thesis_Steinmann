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
	private TypeDescriptor typeDescriptor;
	
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
	
	public void setTypeDescriptor(TypeDescriptor typeDescriptor) {
		this.typeDescriptor = typeDescriptor;
		
	}
	
	public TypeDescriptor getTypeDescriptor(){
		return typeDescriptor;
	}
	
	public String getNameUpperCamel(){
		return getName().substring(0,1).toUpperCase()+getName().substring(1);
	}
	
	/** The type of the field as it appears in c
	 * @return
	 */
	public String getcType(){
		return getcItemType();
	}
	
	/** The type of an item of the list, if the field contains a list.
	 * The normal field type otherwise 
	 * @return
	 */
	public String getcItemType(){
		if (typeDescriptor.isReference()){
			return typeDescriptor.getcName()+"*";
		}
		
		return typeDescriptor.getcName();
	}
	
	/** The type of the field as it appears in java
	 */
	public String getJavaType(){
		return typeDescriptor.getJavaName();
	}
	
	public String getJavaItemType(){
		return typeDescriptor.getJavaName();
	}
}
