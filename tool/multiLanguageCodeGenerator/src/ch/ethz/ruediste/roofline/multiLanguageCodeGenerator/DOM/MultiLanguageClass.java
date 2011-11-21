package ch.ethz.ruediste.roofline.multiLanguageCodeGenerator.DOM;

import java.util.HashMap;
import java.util.LinkedList;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * represents a multi language class
 * 
 * @author ruedi
 * 
 */
@XStreamAlias("class")
public class MultiLanguageClass {

	@XStreamImplicit
	private LinkedList<MultiLanguageFieldBase> fields = new LinkedList<MultiLanguageFieldBase>();

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private String cBaseType;

	@XStreamAsAttribute
	private String javaBaseType;

	@XStreamAsAttribute()
	private String comment;

	public LinkedList<MultiLanguageFieldBase> getFields() {
		return fields;
	}

	public void addField(MultiLanguageFieldBase sharedField) {
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

	public boolean hascBaseType() {
		return cBaseType != null && !cBaseType.isEmpty();
	}

	public void setcBaseType(String cBaseType) {
		this.cBaseType = cBaseType;
	}

	public String getJavaBaseType() {
		return javaBaseType;
	}

	public boolean hasJavaBaseType() {
		return javaBaseType != null && !javaBaseType.isEmpty();
	}

	public void setJavaBaseType(String javaBaseType) {
		this.javaBaseType = javaBaseType;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setTypeDescriptors(
			HashMap<String, FieldTypeDescriptor> typeDescriptors) {
		// can happen due to deserialization
		if (fields == null) {
			return;
		}

		// set the type descriptor of all fields
		for (MultiLanguageFieldBase field : fields) {
			// check if the type of the field is known
			if (!typeDescriptors.containsKey(field.getType())) {
				throw new Error(String.format(
						"Type %s of field %s in class %s not found",
						field.getType(), field.getName(), getName()));
			}

			// set the type descriptor
			field.setTypeDescriptor(typeDescriptors.get(field.getType()));
		}
	}

	public String getNameUpperCamel() {
		return getName().substring(0, 1).toUpperCase() + getName().substring(1);
	}

}