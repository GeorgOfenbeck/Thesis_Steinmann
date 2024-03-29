package ch.ethz.ruediste.roofline.sharedEntityGenerator.DOM;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

/**
 * base class for fields of a multi language class
 * 
 */
@XStreamConverter(value = ToAttributedValueConverter.class, strings = { "comment" })
public abstract class MultiLanguageFieldBase {
	@XStreamAsAttribute
	private String name;
	@XStreamAsAttribute
	private String type;

	@XStreamAsAttribute
	private String comment;

	@XStreamOmitField
	private FieldTypeDescriptor typeDescriptor;

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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setTypeDescriptor(FieldTypeDescriptor typeDescriptor) {
		this.typeDescriptor = typeDescriptor;

	}

	public FieldTypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	public String getNameUpperCamel() {
		return getName().substring(0, 1).toUpperCase() + getName().substring(1);
	}

	/**
	 * The type of the field as it appears in c
	 * 
	 * @return
	 */
	public String getcType() {
		if (typeDescriptor.isReference()) {
			return typeDescriptor.getcName() + "*";
		}

		return typeDescriptor.getcName();
	}

	/**
	 * The type of the field as it appears in java
	 */
	public String getJavaType() {
		return typeDescriptor.getJavaName();
	}

	/**
	 * the default value to be used in java
	 */
	public String getJavaDefault() {
		return typeDescriptor.getJavaDefault();
	}

	public boolean hasJavaDefault() {
		return typeDescriptor.hasJavaDefault();
	}
}
